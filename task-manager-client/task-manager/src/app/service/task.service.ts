import { Injectable } from '@angular/core';
import { ApiService } from './api.service';
import { Task } from '../model/Task';
import { SearchResult } from '../model/SearchResult';

@Injectable({
  providedIn: 'root',
})
export class TaskService {
  constructor(private apiService: ApiService) {}

  getAllTasks(): Promise<Task[]> {
    return this.apiService.get<Task[]>('/api/tasks/all');
  }

  getTenTasks(
    offset: number,
    filter: Task,
    field?: string,
    ascending?: boolean
  ): Promise<SearchResult> {
    let orderString =
      field && field.length != 0
        ? `&orderBy=${field}&ascending=${ascending}`
        : '';
    return this.apiService.post<SearchResult>(
      '/api/tasks/all?offset=' + offset + orderString,
      filter
    );
  }

  getTaskById(id: number): Promise<Task> {
    return this.apiService.get<Task>(`/api/tasks/${id}`);
  }

  createTask(task: Task): Promise<Task> {
    return this.apiService.post<Task>('/api/tasks', task);
  }

  updateTask(id: number, task: Task): Promise<Task> {
    return this.apiService.put<Task>(`/api/tasks/${id}`, task);
  }

  deleteTask(id: number): Promise<void> {
    return this.apiService.delete<void>(`/api/tasks/${id}`);
  }
}
