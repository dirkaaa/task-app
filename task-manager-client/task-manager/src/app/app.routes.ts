import { Routes } from '@angular/router';
import { TaskListComponent } from './component/task-list/task-list.component';
import { EditTaskComponent } from './component/edit-task/edit-task.component';
import { LoginComponent } from './component/login/login.component';

export const routes: Routes = [
  { path: 'tasks', component: TaskListComponent },
  { path: 'edit-task/:id', component: EditTaskComponent },
  { path: 'edit-task', component: EditTaskComponent },
  { path: 'login', component: LoginComponent },
];
