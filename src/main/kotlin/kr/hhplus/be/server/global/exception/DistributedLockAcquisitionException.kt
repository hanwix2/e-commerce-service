package kr.hhplus.be.server.global.exception

class DistributedLockAcquisitionException(keys: String) :
    RuntimeException("Failed to acquire distributed lock for keys: $keys")