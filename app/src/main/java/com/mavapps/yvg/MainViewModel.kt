package com.mavapps.yvg

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mavapps.yvg.dao.ChatDao
import com.mavapps.yvg.model.Chat
import com.mavapps.yvg.model.GF
import com.mavapps.yvg.utils.AITypes
import com.mavapps.yvg.utils.ChatType
import com.mavapps.yvg.utils.genModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.sql.Timestamp

class MainViewModel(
    private val chatDao: ChatDao
) : ViewModel() {
    private val _uiState = MutableStateFlow(UIState())
    val uiState : StateFlow<UIState> = _uiState.asStateFlow()
    private fun updateState(update: (UIState) -> UIState) {
        _uiState.value = update(_uiState.value)
    }

    private val _chats = MutableStateFlow<List<Chat>>(emptyList())
    val chats: StateFlow<List<Chat>> = _chats

    private var chatsJob: Job? = null

    fun observeChats(aiId: Int, userId: Int) {
        chatsJob?.cancel() // cancel previous collection
        chatsJob = viewModelScope.launch {
            chatDao.getByAIandUser(aiId, userId)
                .distinctUntilChanged() // optional, to avoid duplicate lists
                .collect { chats ->
                    Log.d("TAGGER_CHATS", "Im executing ${chats.size}")
                    _chats.value = chats
                }
        }
    }

    fun chats(aiId: Int, userId: Int): StateFlow<List<Chat>> =
        chatDao.getByAIandUser(aiId, userId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    fun onBackButton(screen: String){
        updateState {
            it.copy(currentScreen = screen)
        }
        _chats.value = emptyList()
    }
    
    fun updateCurrentScreen(screen : String){
        updateState {
            it.copy(currentScreen =  screen)
        }
    }

    fun onValueChange(value : String){
        updateState { it.copy(
            userChat = value
        ) }
    }

    fun updateGF(n : Int){
        val gf = AITypes.girlfriendTypes[n]
        updateState {
            it.copy(
                gf = gf
            )
        }
        _chats.value = emptyList()
    }
    fun onUserSubmit(
    ){
        val userId = 1
        val aiId = uiState.value.gf.aiId
        val message = uiState.value.userChat
        viewModelScope.launch {
            val chat = Chat(
                userId = userId,
                aiId = aiId,
                dateTime = Timestamp(System.currentTimeMillis()),
                chatType = ChatType.USERTYPE.id,
                message = message
            )
            chatDao.insert(chat)
        }
        submitMessage()
        updateState { it.copy( userChat = "")}
    }


    fun onDeleteChat(
        chatId : Int
    ){
        viewModelScope.launch {
            chatDao.deleteByChatId(chatId)
        }
    }
    fun submitMessage(
    ){
        val userId = 1
        val aiId = uiState.value.gf.aiId
        val message = uiState.value.userChat
        val gf = AITypes.girlfriendTypes.find { it.aiId == aiId }
        if(gf == null) return
        viewModelScope.launch{
            try {
                Log.d("TAGGER_COMPOSE", "the message is $message")
                val prompt =
                    "You are a virtual girlfriend" +
                            " Your name is: ${gf.aiName}" +
                            " Characteristics: ${gf.attributes}" +
                            " Please reply to any chat of the user." +
                            " This is the chat of the user: $message." +
                            " This is the last 5 history of your chat with the user: ${
                                chats.value.takeLast(5).joinToString(separator = "\n") { it.message }
                            }"

                Log.d("TAGGER_COMPOSE", "The prompt is : ${prompt}")

                Log.d("TAGGER_COMPOSE", "${
                    chats.value.takeLast(5).joinToString(separator = "\n") { it.message }
                }\"")

                val response = genModel.generativeModel.generateContent(prompt)
                val resultText = response.text
                resultText?.let{
                    val chat = Chat(
                        userId = userId,
                        aiId = aiId,
                        dateTime = Timestamp(System.currentTimeMillis()),
                        chatType = ChatType.AITYPE.id,
                        message = resultText
                    )
                    viewModelScope.launch {
                        chatDao.insert(chat)
                    }
                }
            } catch (e: Exception) {
                if(e.message!= null && e.message!!.contains("You exceeded your current quota")){
                    val message = "Bye muna babe, Limit na yung quota mo, Upgrade ka muna sa Pro"
                    val chat = Chat(
                        userId = userId,
                        aiId = aiId,
                        dateTime = Timestamp(System.currentTimeMillis()),
                        chatType = ChatType.AITYPE.id,
                        message = message
                    )
                    viewModelScope.launch {
                        chatDao.insert(chat)
                    }
                }
                Log.e("TAGGER_COMPOSE", "$e")
                e.printStackTrace()
            }
        }
    }
}

data class UIState(
    val userChat : String = "",
    val gf: GF = AITypes.girlfriendTypes[0],
    val currentScreen : String = "selection",
    val chatList: List<Chat> = emptyList()
)

/**

 DATABASE DESIGN
 User(
     userId,
     userName,
     imgUrl,
     firstName,
     lastName
 )

 AI(
     aiId,
     aiName,
     imgUrl,
     attributes
 )

 Chat(
     userId,
     aIId,
     dateTime,
     chatType,
     message
 )


 **/