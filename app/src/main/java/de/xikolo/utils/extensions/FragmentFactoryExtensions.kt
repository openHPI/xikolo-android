package de.xikolo.utils.extensions

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import androidx.fragment.app.FragmentManager

class FragmentFactoryExtension(private val parent: FragmentFactory) : FragmentFactory() {

    val rules: MutableMap<String, () -> Fragment> = mutableMapOf()

    override fun instantiate(classLoader: ClassLoader, className: String): Fragment {
        return rules[className]?.invoke()
            ?: parent.instantiate(classLoader, className)
    }
}

fun <T : FragmentManager> T.registerFragment(id: String, instantiate: () -> Fragment): () -> Fragment {
    if (fragmentFactory !is FragmentFactoryExtension) {
        fragmentFactory = FragmentFactoryExtension(fragmentFactory)
    }
    (fragmentFactory as FragmentFactoryExtension).rules[id] = instantiate
    return instantiate
}
