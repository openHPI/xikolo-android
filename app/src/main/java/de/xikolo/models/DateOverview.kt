package de.xikolo.models

data class DateOverview(var nextDate: CourseDate? = null,
                        var todaysDateCount: Int = 0,
                        var nextSevenDaysDateCount: Int = 0,
                        var futureDateCount: Int = 0)
