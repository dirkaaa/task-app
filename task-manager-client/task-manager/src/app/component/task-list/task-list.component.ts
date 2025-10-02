import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { TaskService } from '../../service/task.service';
import { UserService } from '../../service/user.service';
import { Task } from '../../model/Task';
import { User } from '../../model/User';
import { Status } from '../../model/Status';
import { SearchResult } from '../../model/SearchResult';
import { Router, RouterModule } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatTableModule } from '@angular/material/table';
import { MatIconModule } from '@angular/material/icon';
import { MatNativeDateModule } from '@angular/material/core';
import { MatRadioModule } from '@angular/material/radio';
import { Priority } from '../../model/Priority';

@Component({
  selector: 'app-task-list',
  templateUrl: './task-list.component.html',
  styleUrl: './task-list.component.css',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    RouterModule,
    MatCardModule,
    MatFormFieldModule,
    MatSelectModule,
    MatInputModule,
    MatButtonModule,
    MatDatepickerModule,
    MatTableModule,
    MatIconModule,
    MatNativeDateModule,
    MatRadioModule,
  ],
})
export class TaskListComponent implements OnInit {
  searchForm: FormGroup;
  users: User[] = [];
  tasks: Task[] = [];
  statuses = Object.values(Status);
  priorities = Object.values(Priority);
  loading = false;
  offset = 0;
  numberOfResults = 0;

  constructor(
    private fb: FormBuilder,
    private taskService: TaskService,
    private userService: UserService,
    private router: Router
  ) {
    this.searchForm = this.fb.group({
      assignee: [null],
      creator: [null],
      dueDate: [''],
      status: [''],
      priority: [''],
      description: [''],
      orderBy: [''],
      ascending: ['true'],
    });
  }

  async ngOnInit() {
    await this.searchTasks();
    this.users = await this.userService.getAllUsers();
  }

  async searchTasks() {
    this.loading = true;
    const filter: Task = {
      assignee: this.searchForm.value.assignee,
      creator: this.searchForm.value.creator,
      dueDate:
        this.searchForm.value.dueDate == ''
          ? null
          : this.searchForm.value.dueDate,
      status:
        this.searchForm.value.status == ''
          ? null
          : this.searchForm.value.status,
      priority:
        this.searchForm.value.priority == ''
          ? null
          : this.searchForm.value.priority,
      description: this.searchForm.value.description,
      createdAt: '',
    };
    const result: SearchResult = await this.taskService.getTenTasks(
      this.offset,
      filter,
      this.searchForm.value.orderBy,
      this.searchForm.value.ascending
    );
    this.tasks = result.tasks;
    this.numberOfResults = result.numberOfResults;
    this.loading = false;
  }

  editTask(id: number) {
    this.router.navigate(['/edit-task', id]);
  }

  async deleteTask(id: number) {
    console.log('Deleting task with id:', id);
    await this.taskService.deleteTask(id);
    console.log('Task deleted, refreshing task list');
    await this.searchTasks();
  }

  createTask() {
    this.router.navigate(['/edit-task']);
  }

  nextPage() {
    if (this.offset + 10 < this.numberOfResults) {
      this.offset += 10;
      this.searchTasks();
    }
  }
  previousPage() {
    if (this.offset >= 10) {
      this.offset -= 10;
      this.searchTasks();
    }
  }

  clearFilter() {
    this.searchForm.reset({
      assignee: null,
      creator: null,
      dueDate: '',
      status: '',
      priority: '',
      description: '',
      orderBy: '',
      ascending: ['true'],
    });
    this.offset = 0;
    this.searchTasks();
  }
}
