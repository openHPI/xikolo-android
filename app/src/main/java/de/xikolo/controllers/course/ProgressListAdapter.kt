package de.xikolo.controllers.course

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import de.xikolo.R
import de.xikolo.config.Config
import de.xikolo.models.CourseProgress
import de.xikolo.models.ExerciseStatistic
import de.xikolo.models.SectionProgress
import de.xikolo.models.VisitStatistic
import de.xikolo.views.CustomFontTextView

class ProgressListAdapter(private val context: Context) : RecyclerView.Adapter<ProgressListAdapter.ProgressViewHolder>() {

    companion object {
        val TAG: String = ProgressListAdapter::class.java.simpleName
    }

    private var courseProgress: CourseProgress? = null
    private val sectionProgressList: MutableList<SectionProgress> = mutableListOf()

    fun update(courseProgress: CourseProgress, sectionProgressList: List<SectionProgress>) {
        this.courseProgress = courseProgress
        this.sectionProgressList.clear()
        this.sectionProgressList.addAll(sectionProgressList)
        this.notifyDataSetChanged()
    }

    fun clear() {
        courseProgress = null
        sectionProgressList.clear()
    }

    override fun getItemCount(): Int {
        return if (sectionProgressList.size == 0) {
            0
        } else {
            sectionProgressList.size + 1
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProgressViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_progress, parent, false)
        return ProgressViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProgressViewHolder, position: Int) {
        val title: String
        val mainExercises: ExerciseStatistic?
        val selftestExercises: ExerciseStatistic?
        val bonusExercises: ExerciseStatistic?
        val visits: VisitStatistic?
        if (position == sectionProgressList.size) {
            title = context.getString(R.string.total)
            mainExercises = courseProgress?.mainExercises
            selftestExercises = courseProgress?.selftestExercises
            bonusExercises = courseProgress?.bonusExercises
            visits = courseProgress?.visits
        } else {
            val sectionProgress = sectionProgressList[position]
            title = sectionProgress.title
            mainExercises = sectionProgress.mainExercises
            selftestExercises = sectionProgress.selftestExercises
            bonusExercises = sectionProgress.bonusExercises
            visits = sectionProgress.visits
        }

        holder.textTitle.text = title

        if (selftestExercises != null && selftestExercises.pointsPossible > 0) {
            holder.progressSelftest.visibility = View.VISIBLE
            setupExerciseProgressView(
                holder.progressSelftest,
                selftestExercises.pointsPossible,
                selftestExercises.pointsScored,
                R.string.self_test_points,
                R.string.icon_selftest,
                Config.FONT_XIKOLO
            )
        } else {
            holder.progressSelftest.visibility = View.GONE
        }

        if (mainExercises != null && mainExercises.pointsPossible > 0) {
            holder.progressMain.visibility = View.VISIBLE
            setupExerciseProgressView(
                holder.progressMain,
                mainExercises.pointsPossible,
                mainExercises.pointsScored,
                R.string.assignments_points,
                R.string.icon_assignment,
                Config.FONT_XIKOLO
            )
        } else {
            holder.progressMain.visibility = View.GONE
        }

        if (bonusExercises != null && bonusExercises.pointsPossible > 0) {
            holder.progressBonus.visibility = View.VISIBLE
            setupExerciseProgressView(
                holder.progressBonus,
                bonusExercises.pointsPossible,
                bonusExercises.pointsScored,
                R.string.bonus_points,
                R.string.icon_bonus,
                Config.FONT_XIKOLO
            )
        } else {
            holder.progressBonus.visibility = View.GONE
        }

        if (visits != null && visits.itemsAvailable > 0) {
            holder.progressVisits.visibility = View.VISIBLE
            setupExerciseProgressView(
                holder.progressVisits,
                visits.itemsAvailable.toFloat(),
                visits.itemsVisited.toFloat(),
                R.string.items_visited,
                R.string.icon_visited,
                Config.FONT_MATERIAL
            )
        } else {
            holder.progressVisits.visibility = View.GONE
        }
    }

    private fun setupExerciseProgressView(view: View, base: Float, percent: Float, @StringRes label: Int, @StringRes icon: Int, iconFont: String) {
        val textCount = view.findViewById<TextView>(R.id.text_count)
        val textPercentage = view.findViewById<TextView>(R.id.text_percentage)
        val iconView = view.findViewById<CustomFontTextView>(R.id.icon)
        val progress = view.findViewById<ProgressBar>(R.id.progress)

        iconView.text = context.getString(icon)
        iconView.setCustomFont(context, iconFont)
        textCount.text = String.format(context.getString(label), percent, base)

        val percentage = getPercentage(percent, base)
        textPercentage.text = String.format(context.getString(R.string.percentage), percentage)
        progress.progress = percentage
    }

    private fun getPercentage(state: Float, max: Float): Int {
        return if (max > 0) {
            (state / max * 100).toInt()
        } else {
            100
        }
    }

    class ProgressViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        @BindView(R.id.text_title)
        lateinit var textTitle: TextView

        @BindView(R.id.progress_selftest)
        lateinit var progressSelftest: View

        @BindView(R.id.progress_main)
        lateinit var progressMain: View

        @BindView(R.id.progress_bonus)
        lateinit var progressBonus: View

        @BindView(R.id.progress_visits)
        lateinit var progressVisits: View

        init {
            ButterKnife.bind(this, view)
        }

    }

}
