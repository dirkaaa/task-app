import { Injectable } from '@angular/core';
import { ApiService } from './api.service';
import { User } from '../model/User';
import { UserRegistrationDTO } from '../model/UserRegistrationDTO';

@Injectable({
  providedIn: 'root',
})
export class UserService {
  constructor(private apiService: ApiService) {}

  getAllUsers(): Promise<User[]> {
    return this.apiService.get<User[]>('/api/users/all');
  }

  createUser(user: UserRegistrationDTO): Promise<User> {
    return this.apiService.post<User>('/api/users/register', user);
  }
}
