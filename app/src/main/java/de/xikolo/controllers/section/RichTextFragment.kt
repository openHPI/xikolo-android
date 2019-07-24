package de.xikolo.controllers.section

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import butterknife.BindView
import com.yatatsu.autobundle.AutoBundleField
import de.xikolo.R
import de.xikolo.config.Config
import de.xikolo.controllers.base.NetworkStateFragment
import de.xikolo.controllers.webview.WebViewActivityAutoBundle
import de.xikolo.extensions.observe
import de.xikolo.models.Item
import de.xikolo.utils.LanalyticsUtil
import de.xikolo.utils.MarkdownUtil
import de.xikolo.viewmodels.section.RichTextViewModel

class RichTextFragment : NetworkStateFragment<RichTextViewModel>() {

    companion object {
        val TAG: String = RichTextFragment::class.java.simpleName
    }

    @AutoBundleField
    lateinit var courseId: String

    @AutoBundleField
    lateinit var sectionId: String

    @AutoBundleField
    lateinit var itemId: String

    @BindView(R.id.title)
    lateinit var title: TextView

    @BindView(R.id.text)
    lateinit var text: TextView

    @BindView(R.id.fallback_button)
    lateinit var fallbackButton: Button

    override val layoutResource = R.layout.content_richtext

    private var item: Item? = null

    override fun createViewModel(): RichTextViewModel {
        return RichTextViewModel(itemId)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fallbackButton.setOnClickListener {
            openAsWebView(
                item?.title ?: "",
                Config.HOST_URL + Config.COURSES + courseId + "/" + Config.ITEMS + itemId,
                false,
                false
            )

            LanalyticsUtil.trackRichTextFallback(itemId, courseId, sectionId)
        }

        viewModel.item
            .observe(this) { item ->
                this.item = item
                title.text = item.title

                MarkdownUtil.formatAndSet(viewModel.richText?.text, text)
                showContent()
            }
    }

    private fun openAsWebView(title: String, url: String, inAppLinksEnabled: Boolean, externalLinksEnabled: Boolean) {
        activity?.let {
            val intent = WebViewActivityAutoBundle.builder(title, url)
                .inAppLinksEnabled(inAppLinksEnabled)
                .externalLinksEnabled(externalLinksEnabled)
                .build(it)
            startActivity(intent)
        }
    }

}
