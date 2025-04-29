import { Component, Input, Output, EventEmitter, OnInit } from '@angular/core';
import { MovementKardex } from '../../../../../../model/MovementKardex';
import { MovementKardexService } from '../../../../../../service/movement-kardex.service';
import { TypeKardex } from '../../../../../../model/TypeKardex';
import { TypeKardexService } from '../../../../../../service/type-kardex.service';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-modal-kardex',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './modal-kardex.component.html',
  styleUrls: ['./modal-kardex.component.css']
})
export class ModalKardexComponent implements OnInit {
  @Input() isEdit: boolean = false;
  @Input() movementKardex: MovementKardex = {} as MovementKardex;
  @Input() fixedKardexType: boolean = true;
  
  @Output() closeModal = new EventEmitter<void>();
  @Output() movementUpdated = new EventEmitter<MovementKardex>();

  kardexTypes: TypeKardex[] = [];
  showEntrada: boolean = true;
  selectedKardexType: TypeKardex | null = null;
  
  // Añadimos propiedades para el control de stock actual
  currentStock: number = 0;
  
  // Añadimos propiedad para controlar si los límites de stock son válidos
  stockLimitsExceeded: boolean = false;

  constructor(
    private movementKardexService: MovementKardexService,
    private typeKardexService: TypeKardexService
  ) {}

  ngOnInit(): void {
    this.loadModalKardexTypes();
    this.loadCurrentStock();
  }

  loadModalKardexTypes(): void {
    this.typeKardexService.listAll().subscribe({
      next: (types) => {
        this.kardexTypes = types;
        
        // Si tenemos un typeKardexId en el movimiento, encontrar el objeto de tipo correspondiente
        if (this.movementKardex && this.movementKardex.typeKardexId) {
          this.selectedKardexType = this.kardexTypes.find(
            type => type.id === this.movementKardex.typeKardexId
          ) || null;
        }
      },
      error: (err) => console.error('Error al cargar tipos de Kardex en el modal:', err)
    });
  }

  // Método para cargar el stock actual del producto seleccionado
  loadCurrentStock(): void {
    if (!this.movementKardex || !this.movementKardex.typeKardexId) return;
    
    // Simulamos obtener el último movimiento para este kardex para conocer el stock actual
    this.movementKardexService.getAll().subscribe({
      next: (movements) => {
        // Filtramos por el kardex actual y ordenamos por fecha (descendente)
        const kardexMovements = movements
          .filter(m => m.typeKardexId === this.movementKardex.typeKardexId)
          .sort((a, b) => new Date(b.issueDate).getTime() - new Date(a.issueDate).getTime());
        
        // Si hay movimientos previos, tomamos el saldo del más reciente
        if (kardexMovements.length > 0) {
          this.currentStock = kardexMovements[0].cantidadSaldo || 0;
        } else {
          this.currentStock = 0;
        }
      },
      error: (err) => console.error('Error al cargar el stock actual:', err)
    });
  }

  get modalValorTotalEntrada(): number {
    const { cantidadEntrada = 0, costoUnitarioEntrada = 0 } = this.movementKardex;
    return cantidadEntrada * costoUnitarioEntrada;
  }

  // Obtener el nombre del tipo de kardex seleccionado actualmente
  get selectedKardexName(): string {
    if (this.selectedKardexType) {
      return this.selectedKardexType.name;
    }
    
    // Si aún no hemos cargado los objetos de tipo, pero tenemos un ID
    if (this.movementKardex && this.movementKardex.typeKardexId) {
      const type = this.kardexTypes.find(t => t.id === this.movementKardex.typeKardexId);
      return type ? type.name : 'Cargando...';
    }
    
    return 'Seleccione un tipo';
  }

  // Método para verificar si el stock está dentro de los límites
  checkStockLimits(): boolean {
    if (!this.selectedKardexType) return true;

    const maxAmount = this.selectedKardexType.maximumAmount;
    const minAmount = this.selectedKardexType.minimumQuantity;
    
    // Para entradas, validamos que no se exceda el máximo
    if (this.showEntrada) {
      const newStock = this.currentStock + (this.movementKardex.cantidadEntrada || 0);
      return newStock <= maxAmount;
    } 
    // Para salidas, validamos que no se caiga por debajo del mínimo
    else {
      const newStock = this.currentStock - (this.movementKardex.cantidadSalida || 0);
      return newStock >= minAmount;
    }
  }

  // Actualizar el estado de validación cuando cambian los valores
  updateStockLimitsValidation(): void {
    this.stockLimitsExceeded = !this.checkStockLimits();
    
    // Si los límites se exceden, mostrar una alerta
    if (this.stockLimitsExceeded) {
      if (this.showEntrada) {
        const newStock = this.currentStock + (this.movementKardex.cantidadEntrada || 0);
        Swal.fire({
          title: 'Límite de stock excedido',
          text: `La cantidad ingresada excede el límite máximo permitido (${this.selectedKardexType?.maximumAmount}). El stock resultante sería ${newStock}.`,
          icon: 'warning',
          confirmButtonText: 'Entendido'
        });
      } else {
        const newStock = this.currentStock - (this.movementKardex.cantidadSalida || 0);
        Swal.fire({
          title: 'Stock por debajo del mínimo',
          text: `Esta salida llevará el stock por debajo del mínimo permitido (${this.selectedKardexType?.minimumQuantity}). El stock resultante sería ${newStock}.`,
          icon: 'warning',
          confirmButtonText: 'Entendido'
        });
      }
    }
  }

  // Método para validar los límites de stock
  validateStockLimits(): boolean {
    if (!this.selectedKardexType) return true;

    const maxAmount = this.selectedKardexType.maximumAmount;
    const minAmount = this.selectedKardexType.minimumQuantity;
    
    // Para entradas, validamos que no se exceda el máximo
    if (this.showEntrada) {
      const newStock = this.currentStock + (this.movementKardex.cantidadEntrada || 0);
      if (newStock > maxAmount) {
        Swal.fire({
          title: 'Límite de stock excedido',
          text: `La cantidad ingresada excede el límite máximo permitido (${maxAmount}). El stock resultante sería ${newStock}.`,
          icon: 'warning',
          confirmButtonText: 'Entendido'
        });
        this.stockLimitsExceeded = true;
        return false;
      }
    } 
    // Para salidas, validamos que no se caiga por debajo del mínimo
    else {
      const newStock = this.currentStock - (this.movementKardex.cantidadSalida || 0);
      if (newStock < minAmount) {
        Swal.fire({
          title: 'Stock por debajo del mínimo',
          text: `Esta salida llevará el stock por debajo del mínimo permitido (${minAmount}). El stock resultante sería ${newStock}.`,
          icon: 'warning',
          confirmButtonText: 'Entendido'
        });
        this.stockLimitsExceeded = true;
        return false;
      }
    }
    
    this.stockLimitsExceeded = false;
    return true;
  }

  onSubmitModal(): void {
    if (!this.movementKardex) return;
    
    // Primero validamos los límites de stock
    if (!this.validateStockLimits()) return;
    
    this.proceedWithSubmit();
  }
  
  proceedWithSubmit(): void {
    if (this.isEdit && this.movementKardex.kardexId) {
      this.movementKardexService.update(this.movementKardex.kardexId, this.movementKardex).subscribe({
        next: (updatedMovement) => {
          Swal.fire('Actualizado', 'Movimiento de Kardex actualizado correctamente.', 'success');
          this.movementUpdated.emit(updatedMovement);
          this.closeModal.emit();
        },
        error: (err) => {
          console.error('Error al actualizar el movimiento de Kardex en el modal:', err);
          Swal.fire('Error', 'No se pudo actualizar el movimiento de Kardex.', 'error');
        }
      });
    } else {
      // Calcular el stock actualizado para incluirlo en el movimiento
      if (this.showEntrada) {
        this.movementKardex.cantidadSaldo = this.currentStock + (this.movementKardex.cantidadEntrada || 0);
        // Si no hay un costo unitario de saldo previo, usamos el de entrada
        if (!this.movementKardex.costoUnitarioSaldo) {
          this.movementKardex.costoUnitarioSaldo = this.movementKardex.costoUnitarioEntrada;
        }
        // Calculamos el valor total del saldo
        this.movementKardex.valorTotalSaldo = this.movementKardex.cantidadSaldo * this.movementKardex.costoUnitarioSaldo;
        
        // Limpiamos campos de salida
        this.movementKardex.cantidadSalida = 0;
        this.movementKardex.costoUnitarioSalida = 0;
        this.movementKardex.valorTotalSalida = 0;
      } else {
        this.movementKardex.cantidadSaldo = this.currentStock - (this.movementKardex.cantidadSalida || 0);
        // Mantenemos el costo unitario del saldo
        // Calculamos el valor total de la salida y del saldo
        this.movementKardex.valorTotalSalida = this.movementKardex.cantidadSalida * this.movementKardex.costoUnitarioSalida;
        this.movementKardex.valorTotalSaldo = this.movementKardex.cantidadSaldo * this.movementKardex.costoUnitarioSaldo;
        
        // Limpiamos campos de entrada
        this.movementKardex.cantidadEntrada = 0;
        this.movementKardex.costoUnitarioEntrada = 0;
        this.movementKardex.valorTotalEntrada = 0;
      }
      
      const newMovement = { ...this.movementKardex };
      this.movementKardexService.create(newMovement).subscribe({
        next: (createdMovement) => {
          Swal.fire('Creado', 'Movimiento de Kardex creado correctamente.', 'success');
          this.movementUpdated.emit(createdMovement);
          this.closeModal.emit();
        },
        error: (err) => {
          console.error('Error al crear el movimiento de Kardex en el modal:', err);
          Swal.fire('Error', 'No se pudo crear el movimiento de Kardex.', 'error');
        }
      });
    }
  }
  
  onCancelModal(): void {
    this.movementKardex = {} as MovementKardex;
    this.closeModal.emit();
  }

  toggleEntradaSalida(): void {
    this.showEntrada = !this.showEntrada;
    // Reiniciar la validación cuando cambiamos entre entrada y salida
    this.stockLimitsExceeded = false;
  }
}