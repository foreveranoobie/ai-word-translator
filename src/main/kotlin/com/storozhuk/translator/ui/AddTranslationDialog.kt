package com.storozhuk.translator.ui

import com.github.mvysny.karibudsl.v10.dialog
import com.storozhuk.translator.data.WordDefinitionData
import com.storozhuk.translator.data.WordDefinitionExampleData
import com.storozhuk.translator.data.WordExplanationData
import com.storozhuk.translator.service.WordsService
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.dialog.Dialog
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.textfield.TextArea
import com.vaadin.flow.component.textfield.TextField

class AddTranslationDialog(val wordExplanation: WordExplanationData, val wordsService: WordsService) : Dialog() {

    var isNewAdded = false

    init {
        headerTitle = "Add translation for ${wordExplanation.word}"
        maxWidth = "80%"
        minWidth = "50%"
        maxHeight = "80%"
        val verticalLayout = VerticalLayout()
        val meaningField = TextArea("Add meaning")
        meaningField.isRequiredIndicatorVisible = true
        val usageField = TextArea("Add usage description (optional)")
        val exampleField = TextArea("Add example (optional)")
        val exampleTranslatedField = TextArea("Add example translated to your native language (optional)")
        verticalLayout.add(meaningField)
        verticalLayout.add(usageField)
        verticalLayout.add(exampleField)
        verticalLayout.add(exampleTranslatedField)
        add(verticalLayout)
        val addButton = Button("Add") {
            if (meaningField.value.isBlank()) {
                val notification = Notification("Provide the meaning of the word")
                notification.duration = 3000
                notification.themeName = "error"
                notification.open()
            } else {
                val wordDefinition = WordDefinitionData(meaningField.value, usageField.value,
                    WordDefinitionExampleData(exampleField.value, exampleTranslatedField.value))
                if (wordExplanation.definitions == null) {
                    wordExplanation.definitions = ArrayList()
                }
                wordExplanation.definitions!!.add(wordDefinition)
                wordsService.updateWord(wordExplanation)
                isNewAdded = true
                close()
            }
        }
        addButton.style.setMarginRight("10px")
        val closeButton = Button("Close") {
            close()
        }
        footer.add(closeButton)
        add(addButton)
    }
}