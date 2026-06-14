package com.meal.tracker.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.meal.tracker.di.ServiceLocator
import com.meal.tracker.domain.model.Student
import com.meal.tracker.domain.usecase.StudentManagementUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class StudentViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ServiceLocator.mealRepository
    private val studentManagementUseCase = StudentManagementUseCase(repository)

    val students: StateFlow<List<Student>> = repository.allStudents
        .map { list -> list.map { Student(it.id, it.name, it.orderIndex) } }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _operationResult = MutableSharedFlow<OperationResult>()
    val operationResult: SharedFlow<OperationResult> = _operationResult.asSharedFlow()

    fun addStudent(name: String) = viewModelScope.launch {
        if (name.isBlank()) {
            _operationResult.emit(OperationResult.Error("姓名不能为空"))
            return@launch
        }
        studentManagementUseCase.addStudent(name.trim())
        _operationResult.emit(OperationResult.Success("添加成功"))
    }

    fun addStudents(names: List<String>) = viewModelScope.launch {
        val validNames = names.map { it.trim() }.filter { it.isNotBlank() }
        if (validNames.isEmpty()) {
            _operationResult.emit(OperationResult.Error("没有有效的姓名"))
            return@launch
        }
        studentManagementUseCase.addStudents(validNames)
        _operationResult.emit(OperationResult.Success("批量添加 ${validNames.size} 名学生"))
    }

    fun updateStudent(id: Long, newName: String) = viewModelScope.launch {
        if (newName.isBlank()) {
            _operationResult.emit(OperationResult.Error("姓名不能为空"))
            return@launch
        }
        studentManagementUseCase.updateStudent(id, newName.trim())
        _operationResult.emit(OperationResult.Success("修改成功"))
    }

    fun deleteStudent(id: Long) = viewModelScope.launch {
        studentManagementUseCase.deleteStudent(id)
        _operationResult.emit(OperationResult.Success("删除成功"))
    }

    fun reorderStudents(studentIds: List<Long>) = viewModelScope.launch {
        studentManagementUseCase.reorderStudents(studentIds)
    }
}

sealed class OperationResult {
    data class Success(val message: String) : OperationResult()
    data class Error(val message: String) : OperationResult()
}
