import { Injectable } from '@angular/core';
import { ApiService } from './api.service';
import { Task } from '../model/Task';
import { SearchResult } from '../model/SearchResult';
import { Priority } from '../model/Priority';
import { Status } from '../model/Status';
import { User } from '../model/User';

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

  populateDB(user: User): Promise<void> {
    const tasks: Task[] = [];
    for (let i = 0; i < 25; i++) {
      const date: Date = new Date();
      date.setMonth(new Date().getMonth() + 1);
      date.setDate(i);
      const task: Task = {
        description: `Task ${i}`,
        status: Object.values(Status)[i % Object.values(Status).length],
        priority: Object.values(Priority)[i % Object.values(Priority).length],
        assignee: i % 2 == 0 ? user : null,
        dueDate: date.toISOString(),
      };
      tasks.push(task);
    }

    return tasks.reduce((p, item) => {
      return p.then(() => this.createTask(item)).then(() => {});
    }, Promise.resolve());
  }
}
