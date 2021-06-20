package io.keiji.sample.mastodonclient

import okhttp3.ResponseBody
import retrofit2.http.GET

interface MastodonApi {
    @GET("api/vl/timelines/public")
    suspend fun fetchPublicTimeline(
    ):ResponseBody
}
