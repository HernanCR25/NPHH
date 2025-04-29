import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { NgxPaginationModule } from 'ngx-pagination';
import { ProductService } from '../../../../../service/product.service';
import { Product } from '../../../../../model/Product';
import { SupplierService } from '../../../../../service/supplier.service';
import { Supplier } from '../../../../../model/Supplier';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-product',
  standalone: true,
  imports: [CommonModule, NgxPaginationModule, FormsModule],
  templateUrl: './product.component.html',
  styleUrl: './product.component.css'
})
export class ProductComponent implements OnInit {
  products: Product[] = [];
  filteredProducts: Product[] = [];
  // suppliers: Supplier[] = [];
  currentPage: number = 1;
  itemsPerPage: number = 5;
  totalItems: number = 0;
  statusActive: boolean = true; // Por defecto muestra productos activos
  isLoading: boolean = false;
  
  // Modal properties
  showModal: boolean = false;
  editMode: boolean = false;
  currentProduct: Product = this.getEmptyProduct();

  constructor(
    private productService: ProductService,
    // private supplierService: SupplierService
  ) {}

  ngOnInit(): void {
    this.loadProducts();
    //this.loadSuppliers();
  }

  // Cargar todos los productos
  loadProducts(): void {
    this.isLoading = true;
    this.productService.getAll().subscribe({
      next: (data) => {
        this.products = data;
        this.applyFilters(); // Aplicar filtros al cargar datos
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Error loading products:', err);
        this.isLoading = false;
        Swal.fire('Error', 'No se pudieron cargar los productos', 'error');
      }
    });
  }

  // Cargar todos los proveedores
  // loadSuppliers(): void {
  //  this.supplierService.getAll().subscribe({
  //    next: (data) => {
  //      this.suppliers = data;
  //    },
  //    error: (err) => {
  //      console.error('Error loading suppliers:', err);
  //      Swal.fire('Error', 'No se pudieron cargar los proveedores', 'error');
  //    }
  //  });
  //}

  // Obtener el nombre de la empresa del proveedor por ID
  // getSupplierName(supplierId: number): string {
  //   const supplier = this.suppliers.find(s => s.id === supplierId);
  //  return supplier ? supplier.company : 'No asignado';
  //}

  // Aplicar filtros según el estado seleccionado (activo/inactivo)
  applyFilters(): void {
    // Filtramos según el estado seleccionado (A = Activo, I = Inactivo)
    this.filteredProducts = this.products.filter(p => 
      this.statusActive ? p.status === 'A' : p.status === 'I'
    );
    this.totalItems = this.filteredProducts.length;
    this.currentPage = 1; // Reiniciar a la primera página al filtrar
  }

  // Función para cambiar entre productos activos e inactivos (toggler)
  toggleStatus(): void {
    // Esta función se ejecuta cuando el usuario cambia el switcher
    // statusActive ya se actualiza por [(ngModel)] antes de llamar a esta función
    this.applyFilters(); // Aplicar filtros con el nuevo valor de statusActive
  }

  // Obtener productos paginados para mostrar en la tabla
  get paginatedProducts(): Product[] {
    const start = (this.currentPage - 1) * this.itemsPerPage;
    return this.filteredProducts.slice(start, start + this.itemsPerPage);
  }

  // Cambiar de página en el paginador
  cambiarPagina(nuevaPagina: number): void {
    const totalPages = Math.ceil(this.totalItems / this.itemsPerPage);
    if (nuevaPagina >= 1 && nuevaPagina <= totalPages) {
      this.currentPage = nuevaPagina;
    }
  }

  // Generar array de páginas para el paginador
  getPages(): number[] {
    if (this.totalItems === 0) return [];
    const totalPages = Math.ceil(this.totalItems / this.itemsPerPage);
    
    // Si hay muchas páginas, mostrar solo un número limitado
    if (totalPages > 5) {
      const pages = [];
      // Siempre mostrar primera página
      pages.push(1);
      
      // Mostrar páginas alrededor de la actual
      let startPage = Math.max(2, this.currentPage - 1);
      let endPage = Math.min(totalPages - 1, this.currentPage + 1);
      
      // Agregar ellipsis si es necesario
      if (startPage > 2) {
        pages.push(-1); // Usamos -1 como marcador para mostrar "..."
      }
      
      // Agregar páginas intermedias
      for (let i = startPage; i <= endPage; i++) {
        pages.push(i);
      }
      
      // Agregar ellipsis si es necesario
      if (endPage < totalPages - 1) {
        pages.push(-2); // Usamos -2 como otro marcador para mostrar "..."
      }
      
      // Siempre mostrar última página
      pages.push(totalPages);
      
      return pages;
    }
    
    return Array.from({ length: totalPages }, (_, i) => i + 1);
  }

  // Crear un producto vacío para el formulario
  getEmptyProduct(): Product {
    return {
      id: 0, // Este campo se necesita por la interfaz Product pero será ignorado en el backend
      type: '',
      description: '',
      packageWeight: 0,
      packageQuantity: 0,
      pricePerKg: 0,
      stock: 0,
      entryDate: new Date().toISOString().split('T')[0], // Fecha actual formateada para input date
      expiryDate: '', // Vacío por defecto
      status: 'A', // Activo por defecto
    };
  }

  // Abrir modal para agregar un nuevo producto
  addProduct(): void {
    this.editMode = false;
    this.currentProduct = this.getEmptyProduct();
    this.showModal = true;
  }

  // Abrir modal para editar o crear un producto
  openModal(product?: Product): void {
    if (product) {
      // Modo edición
      this.editMode = true;
      // Crear una copia para no modificar directamente el objeto original
      this.currentProduct = { ...product };
      
      // Formatear correctamente la fecha para que sea un Date
      if (this.currentProduct.entryDate && typeof this.currentProduct.entryDate === 'string') {
        const [year, month, day] = this.currentProduct.entryDate.split('-');
        const entry = new Date(Number(year), Number(month) - 1, Number(day));
        this.currentProduct.entryDate = entry;
      }
      

      if (this.currentProduct.expiryDate) {
        this.currentProduct.expiryDate = new Date(this.currentProduct.expiryDate)
          .toISOString().split('T')[0];
      }
    } else {
      // Modo creación
      this.editMode = false;
      this.currentProduct = this.getEmptyProduct();
    }
    
    this.showModal = true;
  }

  // Cerrar modal
  closeModal(): void {
    this.showModal = false;
  }

  // Guardar producto (crear nuevo o actualizar existente)
  saveProduct(): void {
    this.isLoading = true;
    
    if (this.editMode) {
      // Actualizar producto existente
      this.productService.update(this.currentProduct.id, this.currentProduct).subscribe({
        next: () => {
          this.isLoading = false;
          this.closeModal();
          Swal.fire('Éxito', 'Producto actualizado correctamente', 'success');
          this.loadProducts(); // Recargar productos
        },
        error: (err) => {
          console.error('Error updating product:', err);
          this.isLoading = false;
          Swal.fire('Error', 'No se pudo actualizar el producto', 'error');
        }
      });
    } else {
      // Crear nuevo producto - usamos la misma estructura pero el backend ignorará el id
      // O modificamos directamente el objeto antes de enviarlo para excluir el id
      
      // Opción 1: Omitir el id usando desestructuración
      const { id, ...productWithoutId } = this.currentProduct;
      
      // Opción 2: Crear una copia y usar any para evitar errores de tipo
      // const productWithoutId = { ...this.currentProduct } as any;
      // delete productWithoutId.id;
      
      this.productService.create(productWithoutId as any).subscribe({
        next: () => {
          this.isLoading = false;
          this.closeModal();
          Swal.fire('Éxito', 'Producto creado correctamente', 'success');
          this.loadProducts(); // Recargar productos
        },
        error: (err) => {
          console.error('Error creating product:', err);
          this.isLoading = false;
          Swal.fire('Error', 'No se pudo crear el producto', 'error');
        }
      });
    }
  }

  // Eliminación lógica del producto (cambiar estado a Inactivo)
  softDeleteProduct(id: number): void {
    Swal.fire({
      title: '¿Estás seguro?',
      text: 'El producto será eliminado.',
      icon: 'warning',
      showCancelButton: true,
      confirmButtonColor: '#d33',
      cancelButtonColor: '#3085d6',
      confirmButtonText: 'Sí, eliminar',
      cancelButtonText: 'Cancelar'
    }).then((result) => {
      if (result.isConfirmed) {
        this.isLoading = true;
        this.productService.softDelete(id).subscribe({
          next: () => {
            this.isLoading = false;
            Swal.fire('Eliminado', 'El producto ha sido eliminado.', 'success');
            this.loadProducts(); // Recargar productos
          },
          error: (err) => {
            console.error('Error deleting product:', err);
            this.isLoading = false;
            Swal.fire('Error', 'No se pudo eliminar el producto', 'error');
          }
        });
      }
    });
  }

  // Restaurar producto eliminado lógicamente (cambiar estado a Activo)
  restoreProduct(id: number): void {
    Swal.fire({
      title: '¿Restaurar producto?',
      text: 'El producto volverá a estar disponible.',
      icon: 'info',
      showCancelButton: true,
      confirmButtonColor: '#28a745',
      cancelButtonColor: '#d33',
      confirmButtonText: 'Sí, restaurar',
      cancelButtonText: 'Cancelar'
    }).then((result) => {
      if (result.isConfirmed) {
        this.isLoading = true;
        this.productService.restore(id).subscribe({
          next: () => {
            this.isLoading = false;
            Swal.fire('Restaurado', 'El producto ha sido restaurado.', 'success');
            this.loadProducts(); // Recargar productos
          },
          error: (err) => {
            console.error('Error restoring product:', err);
            this.isLoading = false;
            Swal.fire('Error', 'No se pudo restaurar el producto', 'error');
          }
        });
      }
    });
  }
}