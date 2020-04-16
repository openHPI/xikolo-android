package de.xikolo.views.quiz

import de.xikolo.models.QuizSubmissionAnswer

interface QuestionView {

    fun insertAnswer(answer: QuizSubmissionAnswer?)

    fun showSolution(answer: QuizSubmissionAnswer?)

    fun getAnswer(): QuizSubmissionAnswer?

    fun lock()

    fun unlock()

    var changeListener: () -> Unit
}
