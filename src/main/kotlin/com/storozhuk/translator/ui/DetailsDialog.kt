package com.storozhuk.translator.ui

import com.storozhuk.translator.data.WordExplanationData
import com.storozhuk.translator.service.WordsService
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.dialog.Dialog
import com.vaadin.flow.component.html.Div
import com.vaadin.flow.component.html.Image
import com.vaadin.flow.component.html.ListItem
import com.vaadin.flow.component.html.UnorderedList
import com.vaadin.flow.component.icon.Icon
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout

class DetailsDialog(val wordExplanation: WordExplanationData, val wordsService: WordsService) :
    Dialog() {

    var isDataUpdated = false
    private val definitions = UnorderedList()

    init {
        maxWidth = "80%"
        maxHeight = "80%"
        headerTitle = wordExplanation.word

        val content = VerticalLayout()

        initDefinitionsList()

        definitions.addClassName("custom-layout-grid")

        val addBtnImage = Image("img/add_btn.png", "Add translation")
        addBtnImage.width = "25px"
        addBtnImage.height = "25px"

        val addBtn = Button("Add explanation") {
            val translationDialog = AddTranslationDialog(wordExplanation, wordsService)
            translationDialog.addClassName("popup-dialog")
            translationDialog.open()
            translationDialog.addClosedListener {
                if (translationDialog.isNewAdded) {
                    reinitDefinitionsList()
                    isDataUpdated = true
                }
            }
        }
        addBtn.addClassName("add-explanation-button")
        addBtn.icon = Icon(VaadinIcon.PLUS_CIRCLE)
        content.addClassName("explanation-layout")
        content.add(definitions, addBtn)

        val closeButton = Button("Close") {
            close()
        }
        addClosedListener {
            wordsService.updateWord(wordExplanation)
        }

        add(content)
        footer.add(closeButton)
        addClassName("popup-dialog")
    }

    private fun reinitDefinitionsList() {
        definitions.removeAll()
        initDefinitionsList()
    }

    private fun initDefinitionsList() {
        for (definition in wordExplanation.definitions!!) {
            val row = HorizontalLayout()
            val meaning = UnorderedList()
            val meaningAndUsageItems = ListItem(Div(definition.meaning), Div(definition.usage))
            meaning.add(meaningAndUsageItems)
            val example = UnorderedList()
            example.add(ListItem("Example: ${definition.example!!.foreignLanguage}"))
            example.add(ListItem("Original: ${definition.example.english}"))
            meaning.add(example)
            meaning.addClassName("ul-main")

            val deleteDefinitionBtn = Image("img/red_cross.jpg", "Delete definition")
            deleteDefinitionBtn.width = "20px"
            deleteDefinitionBtn.height = "20px"
            deleteDefinitionBtn.element.addEventListener("mouseover") {
                deleteDefinitionBtn.style.setBackgroundColor("#ededed")
            }
            deleteDefinitionBtn.element.addEventListener("mouseout") {
                deleteDefinitionBtn.style.setBackgroundColor("transparent")
            }
            deleteDefinitionBtn.addClickListener {
                wordExplanation.definitions!!.remove(definition)
                definitions.remove(row)
            }

            val buttonWrapper = Div(deleteDefinitionBtn)
            buttonWrapper.style.setMarginLeft("auto")

            meaning.style.setMarginBottom("15px")
            row.add(meaning, buttonWrapper)
            row.setVerticalComponentAlignment(FlexComponent.Alignment.CENTER, buttonWrapper)
            definitions.add(row)
        }
    }
}