import { Injectable } from '@angular/core';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ApiService {
    private baseUrl = environment.apiUrl;

    private async request<T>(url: string, options: RequestInit = {}): Promise<T> {
        try {
            const response = await fetch(`${this.baseUrl}${url}`, {
                headers: {
                    'Content-Type': 'application/json',
                    ...(options.headers || {})
                },
                credentials: 'include',
                ...options
            });

            if (!response.ok) {
                const errorText = await response.text();
                throw new Error(errorText || `HTTP error ${response.status}`);
            }

            if(options.method !== 'DELETE' && response.status !== 204) {
              return await response.json() as T;
            } else {
              return {} as T;
            }
            
        } catch (error) {
            throw error;
        }
    }

    get<T>(url: string): Promise<T> {
        return this.request<T>(url, { method: 'GET' });
    }

    post<T>(url: string, body: any): Promise<T> {
        return this.request<T>(url, {
            method: 'POST',
            body: JSON.stringify(body)
        });
    }

    put<T>(url: string, body: any): Promise<T> {
        return this.request<T>(url, {
            method: 'PUT',
            body: JSON.stringify(body)
        });
    }

    delete<T>(url: string): Promise<T> {
        return this.request<T>(url, { method: 'DELETE' });
    }

}