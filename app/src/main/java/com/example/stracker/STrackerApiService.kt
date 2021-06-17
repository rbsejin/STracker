package com.example.stracker

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*

private const val BASE_URL = "https://rbsejin.com/"

// Moshi
private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

// 2. Retrofit class
private val retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .baseUrl(BASE_URL)
    .build()

// 1. Service interface
interface STrackerApiService {
    @FormUrlEncoded
    @POST("login.php")
    fun login(@Field("email") email: String, @Field("password") password: String): Call<ResponseDTO>

    @FormUrlEncoded
    @POST("sign_up.php")
    fun signUp(
        @Field("email") email: String,
        @Field("password") password: String
    ): Call<ResponseDTO>

    @FormUrlEncoded
    @POST("email_confirm.php")
    fun emailConfirm(@Field("email") email: String): Call<ResponseDTO>

    @FormUrlEncoded
    @POST("email_authentication.php")
    fun emailConfirmAuthentication(
        @Field("email") email: String,
        @Field("resend") resend: Boolean = false
    ): Call<ResponseDTO>

    @FormUrlEncoded
    @POST("auth_key_confirm.php")
    fun codeConfirm(@Field("email") email: String, @Field("key") key: String): Call<ResponseDTO>

    @FormUrlEncoded
    @POST("password_change.php")
    fun changePassword(
        @Field("email") email: String,
        @Field("password") password: String
    ): Call<ResponseDTO>

    @FormUrlEncoded
    @POST("beginTask.php")
    fun beginTask(
        @Field("email") email: String,
        @Field("task_content") taskContent: String,
        @Field("start_datetime") startDateTime: String,
        @Field("end_datetime") endDateTime: String = ""
    ): Call<TaskTimeDTO>

    @FormUrlEncoded
    @POST("finishTask.php")
    fun finishTask(
        @Field("email") email: String,
        @Field("task_content") taskContent: String,
        @Field("start_datetime") startDateTime: String,
        @Field("end_datetime") endDateTime: String
    ): Call<ResponseDTO>

    @FormUrlEncoded
    @POST("update_task_time.php")
    fun updateTaskTime(
        @Field("email") email: String,
        @Field("task_time_id") taskTimeId: Long,
        @Field("task_content") taskContent: String,
        @Field("start_datetime") startDateTime: String,
        @Field("end_datetime") endDateTime: String
    ): Call<ResponseDTO>

    @FormUrlEncoded
    @POST("delete_task_time.php")
    fun deleteTaskTime(
        @Field("email") email: String,
        @Field("task_time_id") taskTimeId: Long,
    ): Call<ResponseDTO>

    @FormUrlEncoded
    @POST("getTasks.php")
    fun getTasks(@Field("email") email: String): Call<List<TaskDTO>>

    @FormUrlEncoded
    @POST("getTaskTimes.php")
    fun getTaskTimes(@Field("email") email: String): Call<List<TaskTimeDTO>>
}

object STrackerApi {
    val retrofitService: STrackerApiService by lazy {
        retrofit.create(STrackerApiService::class.java)
    }
}