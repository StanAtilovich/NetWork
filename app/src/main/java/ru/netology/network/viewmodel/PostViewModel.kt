package ru.netology.network.viewmodel


import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.netology.network.auth.AppAuth
import ru.netology.network.dto.Coordinates
import ru.netology.network.dto.Event
import ru.netology.network.dto.Job
import ru.netology.network.dto.MediaRequest
import ru.netology.network.dto.Post
import ru.netology.network.enumeration.EventType
import ru.netology.network.model.EventFeedModel
import ru.netology.network.model.FeedModel
import ru.netology.network.model.FeedModelState
import ru.netology.network.model.JobFeedMode
import ru.netology.network.model.PhotoModel
import ru.netology.network.repository.PostRepository
import ru.netology.network.util.SingleLiveEvent
import ru.netology.network.util.convertDateTime2ISO_Instant
import java.io.File
import javax.inject.Inject


private val emptyPost = Post(
    id = 0,
    authorId = 0,
    author = "",
    content = "",
    published = "",
    likedByMe = false,
    coords = null
)

private val emptyEvent = Event(
    id = 0,
    authorId = 0,
    author = "",
    content = "",
    datetime = "",
    published = "",
    type = EventType.OFFLINE,
    likedByMe = false,
    participatedByMe = false,
    ownedByMe = false,
    coords = null
)

private val emptyJob = Job(
    userId = 0,
    id = 0,
    name = "",
    position = "",
    start = "",
    ownedByMe = false
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class PostViewModel @Inject constructor(
    private val repository: PostRepository,
    private val auth: AppAuth,
) : ViewModel() {
    val dataPosts: LiveData<FeedModel> = auth.authStateFlow
        .flatMapLatest { (myId, _) ->
            repository.posts
                .map { posts ->
                    FeedModel(
                        posts.map { it.copy(ownedByMe = it.authorId == myId) },
                        posts.isEmpty()
                    )
                }
        }.asLiveData(Dispatchers.Default)

    val dataEvents: LiveData<EventFeedModel> = auth.authStateFlow
        .flatMapLatest { (myId, _) ->
            repository.events
                .map { event ->
                    EventFeedModel(
                        event.map { it.copy(ownedByMe = it.authorId == myId) },
                        event.isEmpty()
                    )
                }
        }.asLiveData(Dispatchers.Default)
    val dataJobs: LiveData<JobFeedMode> = auth.authStateFlow
        .flatMapLatest { (myId, _) ->
            repository.jobs
                .map { job ->
                    JobFeedMode(
                        job.map {
                            val ownerByMe = it.userId == myId
                            println("=====" + it.userId + " " + myId + " " + ownerByMe)
                            it.copy(ownedByMe = ownerByMe)
                        },
                        job.isEmpty()
                    )
                }
        }.asLiveData(Dispatchers.Default)
    private val editedPost = MutableLiveData(emptyPost)

    private val _dataState = MutableLiveData<FeedModelState>()
    val dataState: LiveData<FeedModelState>
        get() = _dataState

    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated

    private val editedEvent = MutableLiveData(emptyEvent)

    private val _eventCreated = SingleLiveEvent<Unit>()
    val evenCreated: LiveData<Unit>
        get() = _eventCreated

    private val editeJob = MutableLiveData(emptyJob)

    private val _jobCreated = SingleLiveEvent<Unit>()
    val jobCreated: LiveData<Unit>
        get() = _jobCreated

    private val _photo = MutableLiveData(noPhoto)
    val photo: LiveData<PhotoModel>
        get() = _photo

    private val _coords = MutableLiveData(noCoordinates)
    val coords: LiveData<Coordinates>
        get() = _coords

    init {
        loadPosts()
        loadEvents()
    }

    fun loadPosts() = viewModelScope.launch {
        try {
            _dataState.value = FeedModelState(loading = true)
            repository.getPosts()
            _dataState.value = FeedModelState()
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
        }
    }

    fun loadEvents() = viewModelScope.launch {
        try {
            _dataState.value = FeedModelState(loading = true)
            repository.getEvents()
            _dataState.value = FeedModelState()
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
        }
    }

    fun editPosts(post: Post) {
        editedPost.value = post
    }

    fun refreshPosts() = viewModelScope.launch {
        try {
            _dataState.value = FeedModelState(refreshing = true)
            repository.getPosts()
            _dataState.value = FeedModelState()
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
        }
    }

    fun savePosts() {
        editedPost.value?.let { post ->
            _postCreated.value = Unit
            viewModelScope.launch {
                try {
                    when (_photo.value) {
                        noPhoto -> {
                            var postNew = post
                            if (post.attachment != null)
                                postNew = post.copy(attachment = null)
                            repository.save(postNew)
                        }

                        else ->
                            if (_photo.value?.file != null)
                                repository.saveWithAttachment(
                                    post, MediaRequest(_photo.value?.file!!)
                                )
                            else repository.save(post)
                    }
                    _dataState.value = FeedModelState()
                } catch (e: Exception) {
                    _dataState.value = FeedModelState(error = true)
                }
            }
        }
        editedPost.value = emptyPost
        _photo.value = noPhoto
        _coords.value = noCoordinates
    }

    fun changeContentPosts(content: String) {
        val text = content.trim()
        if (editedPost.value?.content == text) {
            return
        }
        editedPost.value = editedPost.value?.copy(content = text)
    }

    fun changeLinkPosts(link: String) {
        val text = if (link.isEmpty())
            null
        else link.trim()
        if (editedPost.value?.link == text) {
            return
        }
        editedPost.value = editedPost.value?.copy(link = text)
    }

    fun changeMentionList(mentionList: String) {
        try {
            val mentionIds = if (mentionList.isNotEmpty())
                mentionList.split(",").map { it.trim().toLong() }
            else emptyList()
            editedPost.value = editedPost.value?.copy(mentionIds = mentionIds)
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
        }
    }

    fun changeCoordsPosts(lat: String?, long: String?) {
        val coords = if (lat == null && long == null || lat == "" && long == "")
            null
        else Coordinates(lat, long)
        if (editedPost.value?.coords == coords) {
            return
        }
        editedPost.value = editedPost.value?.copy(coords = coords)
        editedEvent.value = editedEvent.value?.copy(coords = coords)
    }

    fun changePhoto(uri: Uri?, file: File?) {
        _photo.value = PhotoModel(uri, file)
    }

    fun changeCoordsFromMap(lat: String?, long: String?) {
        if (lat != null) {
            if (long != null) {
                _coords.value = if (lat.isBlank() && long.isBlank())
                    null
                else Coordinates(lat, long)
            }
        }
    }

    fun removePostById(id: Long) = viewModelScope.launch {
        try {
            repository.removeById(id)
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
        }
    }

    fun likePostById(id: Long) = viewModelScope.launch {
        try {
            repository.likeById(id)
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
        }
    }

    fun getEditPost(): Post? {
        return editedPost.value
    }

    fun refreshEvents() = viewModelScope.launch {
        try {
            _dataState.value = FeedModelState(loading = true)
            repository.getEvents()
            _dataState.value = FeedModelState()
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
        }
    }

    fun saveEvent() {
        editedEvent.value?.let { event ->
            _eventCreated.value = Unit
            viewModelScope.launch {
                try {
                    when (_photo.value) {
                        noPhoto -> {
                            var EventNew = event
                            if (event.attachment != null)
                                EventNew = event.copy(attachment = null)
                            repository.saveEvent(EventNew)
                        }

                        else -> {
                            if (_photo.value?.file != null)
                                repository.saveEventWithAttachment(
                                    event,
                                    MediaRequest(_photo.value?.file!!)
                                )
                            else repository.saveEvent(event)
                        }
                    }
                    _dataState.value = FeedModelState()
                } catch (e: Exception) {
                    _dataState.value = FeedModelState(error = true)
                }
            }
        }
        editedEvent.value = emptyEvent
        _photo.value = noPhoto
        _coords.value = noCoordinates
    }

    fun editEvent(event: Event) {
        editedEvent.value = event
    }



    fun changeDateTimeEvent(data: String, time: String) {
        val dataTime = convertDateTime2ISO_Instant(data, time)
        editedEvent.value = editedEvent.value?.copy(datetime = dataTime)
    }

    fun changeContentEvent(content: String) {
        val text = content.trim()
        if (editedEvent.value?.content == text) {
            return
        }
        editedEvent.value = editedEvent.value?.copy(content = text)
    }

    fun changeLinkEvent(link: String) {
        val text = if (link.isEmpty())
            null
        else link.trim()
        if (editedEvent.value?.link == text) {
            return
        }
        editedEvent.value = editedEvent.value?.copy(link = text)
    }

    fun changeCoordsEvent(lat: String?, long: String?) {
        val coords = if (lat == null && long == null || lat == "" && long == "")
            null
        else Coordinates(lat, long)
        if (editedEvent.value?.coords == coords) {
            return
        }
        editedEvent.value = editedEvent.value?.copy(coords = coords)
    }

    fun changeSpeakersEvent(speakersStr: String) {
        if (speakersStr.isNotEmpty())
            try {
                val speakers = speakersStr.split(",").map {
                    it.trim().toLong()
                }
                editedEvent.value = editedEvent.value?.copy(speakerIds = speakers)
            } catch (e: Exception) {
                _dataState.value = FeedModelState(error = true)
            }
    }

    fun changeTypeEvent(isOnLine: Boolean) {
        if (isOnLine)
            editedEvent.value = editedEvent.value?.copy(type = EventType.ONLINE)
        else
            editedEvent.value = editedEvent.value?.copy(type = EventType.OFFLINE)
    }

    fun removeEventById(id: Long) = viewModelScope.launch {
        try {
            repository.removeEventById(id)
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
        }
    }

    fun likeEventById(id: Long, likedByMe: Boolean) = viewModelScope.launch {
        try {
            repository.likeEventById(id, likedByMe)
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
        }
    }

    fun getEditEvent(): Event? {
        return editedEvent.value
    }

    fun participate(id: Long, participatedByMe: Boolean) = viewModelScope.launch {
        try {
            repository.partEventById(id, participatedByMe)
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
        }
    }

    fun getCurrentUser(): Long {
        return auth.authStateFlow.value.id
    }

    fun loadJobs(userId: Long, currentUserId: Long) = viewModelScope.launch {
        try {
            _dataState.value = FeedModelState(loading = true)
            repository.getJobs(userId, currentUserId)
            _dataState.value = FeedModelState()
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
        }
    }

    fun editJob(job: Job) {
        editeJob.value = job
    }

    fun getEditJob(): Job? {
        return editeJob.value
    }

    fun saveJob(userId: Long) {
        editeJob.value?.let { job ->
            _jobCreated.value = Unit
            viewModelScope.launch {
                try {
                    repository.saveJob(userId, job)
                    _dataState.value = FeedModelState()
                } catch (e: Exception) {
                    _dataState.value = FeedModelState(error = true)
                }
            }
        }
        editeJob.value = emptyJob
    }



    fun changeJobStart(start: String) {
        val dataStart = convertDateTime2ISO_Instant(start, "00:00")
        editeJob.value = editeJob.value?.copy(start = dataStart)
    }

    fun changeJobFinish(finish: String) {
        val finishString =
            if (finish.isNotEmpty()) convertDateTime2ISO_Instant(finish, "00:00") else null
        editeJob.value = editeJob.value?.copy(finish = finishString)
    }

    fun changeNameJob(name: String) {
        val text = name.trim()
        if (editeJob.value?.name == text) {
            return
        }
        editeJob.value = editeJob.value?.copy(name = text)
    }

    fun changePositionJob(position: String) {
        val text = position.trim()
        if (editeJob.value?.name == text) {
            return
        }
        editeJob.value = editeJob.value?.copy(position = text)
    }

    fun changeLinkJob(link: String) {
        val text = if (link.isEmpty())
            null
        else
            link.trim()
        if (editeJob.value?.link == text) {
            return
        }
        editeJob.value = editeJob.value?.copy(link = text)
    }

    fun removeJobById(id: Long) = viewModelScope.launch {
        try {
            repository.removeJobById(id)
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
        }
    }

    fun refreshJobs(userId: Long, currentUserId: Long) = viewModelScope.launch {
        try {
            _dataState.value = FeedModelState(refreshing = true)
            repository.getJobs(userId, currentUserId)
            _dataState.value = FeedModelState(error = true)
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
        }
    }

}


private val noPhoto = PhotoModel()
private val noCoordinates = Coordinates()

