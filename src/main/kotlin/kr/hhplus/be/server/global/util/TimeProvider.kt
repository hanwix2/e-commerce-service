package kr.hhplus.be.server.global.util

import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Component
class TimeProvider {

    fun getCurrentDate(): LocalDate {
        return LocalDate.now()
    }

    fun getCurrentIsoStringDate(): String {
        return getCurrentDate().format(DateTimeFormatter.BASIC_ISO_DATE)
    }

}