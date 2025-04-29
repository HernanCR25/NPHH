import { Component, OnInit, OnDestroy } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { HttpClientModule } from '@angular/common/http';
import { MENU_ITEMS } from '../../utils/menu-items';

@Component({
  selector: 'app-sidemenu',
  standalone: true,
  imports: [CommonModule, RouterModule, HttpClientModule],
  templateUrl: './sidemenu.component.html',
})
export class SidemenuComponent implements OnInit, OnDestroy {
  dropdownIndex: number | null = null;
  subDropdownIndex: Map<number, number | null> = new Map();
  grandSubDropdownIndex: Map<number, Map<number, number | null>> = new Map();
  menuItems = MENU_ITEMS;
  isSidebarOpen = false;
  
  // Para la responsividad
  private resizeListener: any;
  private sidebarToggleListener: any;

  constructor(private router: Router) { }

  ngOnInit(): void {
    // Escuchar el evento del navbar
    this.sidebarToggleListener = this.listenForSidebarToggle();
    
    // Escuchar cambios de tamaño de pantalla
    this.resizeListener = this.handleResize();
    
    // Comprobar tamaño inicial
    this.checkScreenSize();
  }

  ngOnDestroy(): void {
    // Eliminar event listeners cuando se destruye el componente
    window.removeEventListener('resize', this.resizeListener);
    window.removeEventListener('sidebar-toggle', this.sidebarToggleListener);
  }
  
  private listenForSidebarToggle(): any {
    const listener = (event: any) => {
      if (event.detail && event.detail.hasOwnProperty('isOpen')) {
        this.isSidebarOpen = event.detail.isOpen;
      }
    };
    
    window.addEventListener('sidebar-toggle', listener);
    return listener;
  }
  
  private handleResize(): any {
    const listener = () => {
      this.checkScreenSize();
    };
    
    window.addEventListener('resize', listener);
    return listener;
  }
  
  private checkScreenSize(): void {
    // En pantallas grandes (lg), el sidebar está siempre visible
    if (window.innerWidth >= 1024) {
      this.isSidebarOpen = true;
    } else {
      // En móvil, el sidebar está oculto por defecto
      this.isSidebarOpen = false;
    }
  }

  toggleDropdown(index: number): void {
    this.dropdownIndex = this.dropdownIndex === index ? null : index;
  }

  toggleSubDropdown(parentIndex: number, childIndex: number): void {
    const current = this.subDropdownIndex.get(parentIndex);
    this.subDropdownIndex.set(parentIndex, current === childIndex ? null : childIndex);
  }

  toggleGrandSubDropdown(parentIndex: number, childIndex: number, grandChildIndex: number): void {
    if (!this.grandSubDropdownIndex.has(parentIndex)) {
      this.grandSubDropdownIndex.set(parentIndex, new Map());
    }
    const subMap = this.grandSubDropdownIndex.get(parentIndex)!;
    const current = subMap.get(childIndex);
    subMap.set(childIndex, current === grandChildIndex ? null : grandChildIndex);
  }
  
  closeSidebar(): void {
    this.isSidebarOpen = false;
  }

  isRouteActive(path: string): boolean {
    return this.router.url.startsWith(path);
  }

  logout(): void {
    console.log('Sesión cerrada');
    localStorage.removeItem('authToken');
    this.router.navigate(['/login']);
  }
}