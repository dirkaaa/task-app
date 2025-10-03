import { User } from './User';
import { Status } from './Status';
import { Priority } from './Priority';
import { Category } from './Category';

export interface Task {
  id?: number;
  description?: string;
  status?: Status;
  priority?: Priority;
  assignee?: User | null;
  creator?: User;
  dueDate?: string;
  createdAt?: string;
  category?: Category | null;
}
