import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { TaskService } from '../../service/task.service';
import { UserService } from '../../service/user.service';
import { Task } from '../../model/Task';
import { User } from '../../model/User';
import { Status } from '../../model/Status';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';

@Component({
  selector: 'app-edit-task',
  templateUrl: './edit-task.component.html',
  styleUrl: './edit-task.component.css',
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
    MatNativeDateModule
  ]
})
export class EditTaskComponent implements OnInit {
  taskForm: FormGroup;
  users: User[] = [];
  statuses = Object.values(Status);
  isEditMode = false;
  loading = false;
  creator: User | null = null;

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private taskService: TaskService,
    private userService: UserService
  ) {
    this.taskForm = this.fb.group({
      description: ['', Validators.required],
      status: ['', Validators.required],
      assignee: [null],
      creator: [{ value: '', disabled: true }],
      dueDate: ['', Validators.required],
      createdAt: ['']
    });
  }

  async ngOnInit() {
    this.users = await this.userService.getAllUsers();
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isEditMode = true;
      const task = await this.taskService.getTaskById(+id);
      this.creator = task.creator ?? null;
      this.taskForm.patchValue({
        description: task.description,
        status: task.status,
        assignee: task.assignee,
        creator: task.creator?.username,
        dueDate: task.dueDate ? new Date(task.dueDate) : '',
        createdAt: task.createdAt
      });
    }
  }

  async onSubmit() {
    if (this.taskForm.invalid) {
      this.taskForm.markAllAsTouched();
      return;
    }
    this.loading = true;
    const formValue = this.taskForm.getRawValue();
    const task: Task = {
      description: formValue.description,
      status: formValue.status,
      assignee: formValue.assignee,
      creator: this.creator ?? undefined,
      dueDate: this.formatLocalDate(formValue.dueDate),
      createdAt: formValue.createdAt
    };
    console.log('Submitting task:', task);
    try {
      if (this.isEditMode && this.route.snapshot.paramMap.get('id')) {
        await this.taskService.updateTask(+this.route.snapshot.paramMap.get('id')!, task);
      } else {
        console.log('Creating new task:', task);
        await this.taskService.createTask(task);
      }
      this.router.navigate(['/tasks']);
    } finally {
      this.loading = false;
    }
  }

  formatLocalDate(d: Date): string {
  const year = d.getFullYear();
  const month = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
}
}