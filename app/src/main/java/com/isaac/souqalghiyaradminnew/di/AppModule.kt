package com.isaac.souqalghiyaradminnew.di

import com.google.firebase.firestore.FirebaseFirestore
import com.isaac.souqalghiyaradminnew.data.repository.*
import com.isaac.souqalghiyaradminnew.domain.repository.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    // تمت إضافة الـ AdminRepository هنا
    @Provides
    @Singleton
    fun provideAdminRepository(db: FirebaseFirestore): AdminRepository = AdminRepositoryImpl(db)

    @Provides
    @Singleton
    fun provideOrdersRepository(db: FirebaseFirestore): OrdersRepository = OrdersRepositoryImpl(db)

    @Provides
    @Singleton
    fun provideAdsRepository(db: FirebaseFirestore): AdsRepository = AdsRepositoryImpl(db)

    @Provides
    @Singleton
    fun provideConstantsRepository(db: FirebaseFirestore): ConstantsRepository = ConstantsRepositoryImpl(db)
}
