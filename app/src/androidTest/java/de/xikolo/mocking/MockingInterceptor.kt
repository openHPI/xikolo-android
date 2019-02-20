package de.xikolo.mocking

import android.content.Context
import de.xikolo.BuildConfig
import okhttp3.*

/**
 * An Interceptor which mocks request responses.
 * It gets a mocked response object from the {@link MockingData} class and constructs a response.
 * If an exception occurs while handling, the interceptor will stop intercepting by forwarding the request to the next object in the interception chain.
 */
class MockingInterceptor(private val context: Context) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        return try {
            val mockedResponse = MockingData.getResponse(context, chain.request(), BuildConfig.X_FLAVOR)!!
            Response.Builder()
                .code(mockedResponse.statusCode)
                .message(mockedResponse.responseString)
                .request(chain.request())
                .protocol(Protocol.HTTP_1_1)
                .body(
                    ResponseBody.create(
                        MediaType.parse(mockedResponse.contentType),
                        mockedResponse.responseString.toByteArray()
                    )
                )
                .addHeader("content-type", mockedResponse.contentType)
                .build()
        } catch (e: Exception) {
            // make a regular request
            chain.proceed(chain.request())
        }
    }

}
