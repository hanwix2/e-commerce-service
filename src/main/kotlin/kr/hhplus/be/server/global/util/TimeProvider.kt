package kr.hhplus.be.server.global.util

import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class TimeProvider {
    fun getCurrentDate(): LocalDate {
        return LocalDate.now()
    }
}