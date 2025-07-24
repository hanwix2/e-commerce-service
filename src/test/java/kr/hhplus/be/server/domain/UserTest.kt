package kr.hhplus.be.server.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class UserTest {

    @Test
    fun chargePoint() {
        val user = User()
        val chargeAmount = 500L

        user.chargePoint(chargeAmount)

        assertEquals(chargeAmount, user.point)
    }

    @Test
    fun chargePointWithMinusValue() {
        val user = User()

        assertThrows<IllegalArgumentException> {
            user.chargePoint(-100L)
        }
    }

}