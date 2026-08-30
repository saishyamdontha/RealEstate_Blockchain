// SPDX-License-Identifier: MIT
pragma solidity ^0.8.24;

contract PropertyLedger {
    enum Status { Listed, EarnestDeposited, InspectionPassed, InspectionFailed, Finalized, Cancelled }

    struct Sale {
        address seller;
        address buyer;
        address lender;
        address inspector;
        uint256 price;
        uint256 earnestAmount;
        uint256 depositedByBuyer;
        uint256 depositedByLender;
        Status status;
    }

    uint256 public nextPropertyId;
    mapping(uint256 => Sale) public sales;
    mapping(uint256 => address) public propertyOwner;

    event PropertyRegistered(uint256 indexed propertyId, address indexed owner);
    event Listed(uint256 indexed propertyId, address indexed seller, uint256 price, uint256 earnestAmount);
    event EarnestDeposited(uint256 indexed propertyId, address indexed buyer, uint256 amount);
    event InspectionUpdated(uint256 indexed propertyId, bool passed);
    event LenderFunded(uint256 indexed propertyId, address indexed lender, uint256 amount);
    event PropertyTransferred(uint256 indexed propertyId, address indexed from, address indexed to);
    event SaleCancelled(uint256 indexed propertyId, address indexed cancelledBy, bool earnestForfeited);

    error NotSeller(address caller);
    error NotBuyer(address caller);
    error NotLender(address caller);
    error NotInspector(address caller);
    error WrongStatus(Status current, Status expected);
    error IncorrectValue(uint256 sent, uint256 expected);
    error InsufficientFunds(uint256 have, uint256 needed);
    error TransferFailed();
    error PropertyDoesNotExist(uint256 propertyId);

    modifier propertyExists(uint256 propertyId) {
        if (propertyOwner[propertyId] == address(0)) revert PropertyDoesNotExist(propertyId);
        _;
    }

    function registerProperty() external returns (uint256 propertyId) {
        propertyId = nextPropertyId++;
        propertyOwner[propertyId] = msg.sender;
        emit PropertyRegistered(propertyId, msg.sender);
    }

    function listProperty(
        uint256 propertyId,
        address buyer,
        address lender,
        address inspector,
        uint256 price,
        uint256 earnestAmount
    ) external propertyExists(propertyId) {
        if (propertyOwner[propertyId] != msg.sender) revert NotSeller(msg.sender);
        if (earnestAmount > price) revert IncorrectValue(earnestAmount, price);

        sales[propertyId] = Sale({
            seller: msg.sender,
            buyer: buyer,
            lender: lender,
            inspector: inspector,
            price: price,
            earnestAmount: earnestAmount,
            depositedByBuyer: 0,
            depositedByLender: 0,
            status: Status.Listed
        });

        emit Listed(propertyId, msg.sender, price, earnestAmount);
    }

    function depositEarnest(uint256 propertyId) external payable propertyExists(propertyId) {
        Sale storage sale = sales[propertyId];
        if (msg.sender != sale.buyer) revert NotBuyer(msg.sender);
        if (sale.status != Status.Listed) revert WrongStatus(sale.status, Status.Listed);
        if (msg.value != sale.earnestAmount) revert IncorrectValue(msg.value, sale.earnestAmount);

        sale.depositedByBuyer += msg.value;
        sale.status = Status.EarnestDeposited;
        emit EarnestDeposited(propertyId, msg.sender, msg.value);
    }

    function updateInspectionStatus(uint256 propertyId, bool passed) external propertyExists(propertyId) {
        Sale storage sale = sales[propertyId];
        if (msg.sender != sale.inspector) revert NotInspector(msg.sender);
        if (sale.status != Status.EarnestDeposited) revert WrongStatus(sale.status, Status.EarnestDeposited);
        sale.status = passed ? Status.InspectionPassed : Status.InspectionFailed;
        emit InspectionUpdated(propertyId, passed);
    }

    function fundAsLender(uint256 propertyId) external payable propertyExists(propertyId) {
        Sale storage sale = sales[propertyId];
        if (msg.sender != sale.lender) revert NotLender(msg.sender);
        if (sale.status != Status.InspectionPassed) revert WrongStatus(sale.status, Status.InspectionPassed);
        uint256 remaining = sale.price - sale.earnestAmount;
        if (msg.value != remaining) revert IncorrectValue(msg.value, remaining);

        sale.depositedByLender += msg.value;
        emit LenderFunded(propertyId, msg.sender, msg.value);
    }

    function finalizeSale(uint256 propertyId) external propertyExists(propertyId) {
        Sale storage sale = sales[propertyId];
        if (sale.status != Status.InspectionPassed) revert WrongStatus(sale.status, Status.InspectionPassed);
        uint256 totalCollected = sale.depositedByBuyer + sale.depositedByLender;
        if (totalCollected < sale.price) revert InsufficientFunds(totalCollected, sale.price);

        sale.status = Status.Finalized;
        address seller = sale.seller;
        address buyer = sale.buyer;
        uint256 price = sale.price;

        propertyOwner[propertyId] = buyer;

        (bool success, ) = payable(seller).call{value: price}("");
        if (!success) revert TransferFailed();

        emit PropertyTransferred(propertyId, seller, buyer);
    }

    function cancelSale(uint256 propertyId) external propertyExists(propertyId) {
        Sale storage sale = sales[propertyId];
        if (msg.sender != sale.buyer && msg.sender != sale.seller) revert NotBuyer(msg.sender);
        if (sale.status == Status.Finalized || sale.status == Status.Cancelled) {
            revert WrongStatus(sale.status, Status.EarnestDeposited);
        }

        bool forfeit = sale.status == Status.InspectionPassed;
        uint256 buyerRefund = forfeit ? 0 : sale.depositedByBuyer;
        uint256 sellerPayout = forfeit ? sale.depositedByBuyer : 0;
        uint256 lenderRefund = sale.depositedByLender;

        sale.status = Status.Cancelled;
        sale.depositedByBuyer = 0;
        sale.depositedByLender = 0;

        if (buyerRefund > 0) {
            (bool ok, ) = payable(sale.buyer).call{value: buyerRefund}("");
            if (!ok) revert TransferFailed();
        }
        if (sellerPayout > 0) {
            (bool ok, ) = payable(sale.seller).call{value: sellerPayout}("");
            if (!ok) revert TransferFailed();
        }
        if (lenderRefund > 0) {
            (bool ok, ) = payable(sale.lender).call{value: lenderRefund}("");
            if (!ok) revert TransferFailed();
        }

        emit SaleCancelled(propertyId, msg.sender, forfeit);
    }
}
