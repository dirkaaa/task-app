import { Injectable } from '@angular/core';
import { ApiService } from './api.service';
import { Category } from '../model/Category';

@Injectable({
  providedIn: 'root',
})
export class CategoryService {
  constructor(private apiService: ApiService) {}

  getEveryCategory(): Promise<Category[]> {
    return this.apiService.get<Category[]>('/api/categories');
  }

  getCategoryById(id: number): Promise<Category> {
    return this.apiService.get<Category>(`/api/categories/${id}`);
  }

  createCategory(category: Category): Promise<Category> {
    return this.apiService.post<Category>(`/api/categories`, category);
  }

  deleteCategory(id: number): Promise<void> {
    return this.apiService.delete<void>(`/api/categories/${id}`);
  }
}
