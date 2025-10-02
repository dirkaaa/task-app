import { Component } from '@angular/core';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from './service/auth.service';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterModule, MatToolbarModule, MatButtonModule],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css',
})
export class AppComponent {
  constructor(public authService: AuthService, private router: Router) {}

  isLoggedIn(): boolean {
    console.log('Is user logged in?', this.authService.isUserLogedIn());
    return this.authService.isUserLogedIn();
  }

  getCurrentUserName(): string | null {
    const user = this.authService.getCurrentUser();
    return user ? user.username : null;
  }

  logout() {
    this.authService.logout().finally(() => {
      this.router.navigate(['/login']);
      localStorage.removeItem('user');
    });
  }
}
