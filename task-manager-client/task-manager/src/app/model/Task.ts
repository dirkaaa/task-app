import { User } from "./User";
import { Status } from "./Status";

export interface Task {
    id?: number;
    description?: string;
    status?: Status;
    assignee?: User | null;
    creator?: User;
    dueDate?: string;      // ISO string, e.g. "2025-09-30"
    createdAt?: string;    // ISO string, e.g. "2025-09-30"
}