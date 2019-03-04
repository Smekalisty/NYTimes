package com.nytimes.contracts

import com.nytimes.entities.Constants
import com.nytimes.entities.pojo.Post
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface API {
    @GET("svc/mostpopular/v2/mostviewed/all-sections/{period}.json")
    fun posts(@Path("period") period: String, @Query(Constants.apiKeyParameter) apiKey: String): Single<ArrayList<Post>>
}