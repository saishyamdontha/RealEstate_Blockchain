// SPDX-License-Identifier: MIT
pragma solidity ^0.8.19;

/// @title PropertyLedger — reconstructed from the existing web3j wrapper's
///        function signatures (recordTransfer, getTransfersCount, transfers,
///        PropertyTransferred event). The original .sol source was never
///        committed to the repo — only the compiled wrapper survived.
contract PropertyLedger {
    struct Transfer {
        uint256 propertyId;
        string sellerId;
        string buyerId;
        string txHash;
        uint256 timestamp;
    }

    Transfer[] public transfers;

    event PropertyTransferred(
        uint256 propertyId,
        string sellerId,
        string buyerId,
        string txHash,
        uint256 timestamp
    );

    function recordTransfer(
        uint256 propertyId,
        string memory sellerId,
        string memory buyerId,
        string memory txHash
    ) public {
        transfers.push(Transfer(propertyId, sellerId, buyerId, txHash, block.timestamp));
        emit PropertyTransferred(propertyId, sellerId, buyerId, txHash, block.timestamp);
    }

    function getTransfersCount() public view returns (uint256) {
        return transfers.length;
    }
}
