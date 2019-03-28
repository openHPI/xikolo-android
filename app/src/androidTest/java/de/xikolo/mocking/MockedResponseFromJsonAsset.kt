package de.xikolo.mocking

import android.content.Context
import de.xikolo.mocking.base.BaseMockedResponse

/**
 * Represents a mocked response read from a .json file in the app's assets directory.
 * The {@code assetPath} only specifies the name of the resource, without the .json file ending.
 */
class MockedResponseFromJsonAsset(context: Context, assetPath: String) : BaseMockedResponse() {

    override var responseString: String = context.assets.open("$assetPath.json").bufferedReader().use {
        val response = it.readText()
        it.close()
        response
    }

    override var statusCode: Int = 200

    override var contentType: String = "application/json"

}
