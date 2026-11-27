package org.example.project.feature.vocabulary.di

import org.example.project.feature.vocabulary.data.network.VocabularyApi
import org.example.project.feature.vocabulary.data.repository.VocabularyRepositoryImpl
import org.example.project.feature.vocabulary.domain.repository.VocabularyRepository
import org.example.project.feature.vocabulary.presentation.list.VocabularyViewModel
import org.example.project.feature.vocabulary.presentation.note.WordNoteViewModel
import org.example.project.feature.vocabulary.presentation.word.VocabularyWordViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val vocabularyModule = module {
    single { VocabularyApi(client = get(), config = get()) }
    single<VocabularyRepository> { VocabularyRepositoryImpl(api = get(), sessionStore = get()) }
    viewModelOf(::VocabularyViewModel)
    viewModelOf(::VocabularyWordViewModel)
    viewModelOf(::WordNoteViewModel)
}
