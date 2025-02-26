package misk.slack

import com.google.inject.Provides
import com.squareup.moshi.Moshi
import misk.client.HttpClientEndpointConfig
import misk.client.HttpClientFactory
import misk.inject.KAbstractModule
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import wisp.moshi.buildMoshi
import javax.inject.Named
import javax.inject.Singleton

/**
 * Installs the Slack webhook client.
 * This should be installed once per service and enables imported libraries to post to Slack using
 * the service level config.
 */
class SlackModule(private val config: SlackConfig) : KAbstractModule() {
  override fun configure() {
    bind<SlackConfig>().toInstance(config)
    bind<SlackClient>().to(RealSlackClient::class.java)
  }

  @Provides @Singleton fun provideSlackWebhookApi(
    httpClientFactory: HttpClientFactory,
    @Named("misk-slack") moshi: Moshi
  ): SlackWebhookApi {
    val okHttpClient = httpClientFactory.create(
        HttpClientEndpointConfig(url = config.baseUrl))
    val retrofit = Retrofit.Builder()
        .baseUrl(config.baseUrl)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .client(okHttpClient)
        .build()
    return retrofit.create(SlackWebhookApi::class.java)
  }

  @Provides @Singleton @Named("misk-slack") fun provideMoshi(): Moshi {
    return buildMoshi(emptyList())
  }
}
