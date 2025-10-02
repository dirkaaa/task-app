import { Injectable } from '@angular/core';
import { ApiService } from './api.service';
import { Credentials } from '../model/Credentials';
import { User } from '../model/User';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  constructor(private apiService: ApiService) {}

  login(credentials: Credentials): Promise<User> {
    return this.apiService.post<User>('/api/auth/login', credentials);
  }

  logout(): Promise<void> {
    return this.apiService.delete<void>('/api/auth/logout');
  }

  isUserLogedIn(): boolean {
    return !!localStorage.getItem('user');
  }

  getCurrentUser(): User | null {
    const userJson = localStorage.getItem('user');
    return userJson ? (JSON.parse(userJson) as User) : null;
  }
}
