import { Task } from './Task';

export interface SearchResult {
  numberOfResults: number;
  currentOffset: number;
  tasks: Task[];
}
