package br.com.benhurzambiasi.createreactliferay.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.serviceOrNull
import com.intellij.util.xmlb.XmlSerializerUtil

@State(
    name = "CreateReactLiferaySettings",
    storages = [Storage("create-react-liferay.xml")]
)
class CreateReactLiferaySettings : PersistentStateComponent<CreateReactLiferaySettings> {
    var nodePath: String = ""

    override fun getState(): CreateReactLiferaySettings = this

    override fun loadState(state: CreateReactLiferaySettings) {
        XmlSerializerUtil.copyBean(state, this)
    }

    companion object {
        private val fallback = CreateReactLiferaySettings()

        fun getInstance(): CreateReactLiferaySettings {
            val app = ApplicationManager.getApplication() ?: return fallback
            return app.serviceOrNull<CreateReactLiferaySettings>() ?: fallback
        }
    }
}

