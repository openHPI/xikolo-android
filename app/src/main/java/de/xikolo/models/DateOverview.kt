package de.xikolo.models

data class DateOverview(var nextDate: CourseDate? = null,
                        var countToday: Long = 0,
                        var countNextSevenDays: Long = 0,
                        var countFuture: Long = 0)
