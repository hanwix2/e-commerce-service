package kr.hhplus.be.server.domain

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ProductRepository : JpaRepository<Product, Long> {

    fun findAllByIdIn(ids: List<Long>): List<Product>

}