import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';
import { Vaccine } from '../../../../../model/Vaccine';
import { Shed } from '../../../../../model/Shed';
import { VaccineService } from '../../../../../service/vaccine.service';
import { ShedService } from '../../../../../service/shed.service';
import { VaccineApplicationsService } from '../../../../../service/vaccineAplications';
import { formatDate } from '@angular/common';
import { VaccineApplications } from '../../../../../model/VaccineAplications';
import { CicloVida } from '../../../../../model/Lifecycle';
import { CicloVidaService } from '../../../../../service/lifecycle.service';


@Component({
  standalone: true,
  imports: [CommonModule, HttpClientModule, FormsModule],
  templateUrl: './vaccine-aplications.component.html',
  styles: []
})
export class VaccineApplicationsComponent implements OnInit {
  isModalOpen = false;
  applications: VaccineApplications[] = [];
  filteredApplications: VaccineApplications[] = [];
  sheds: Shed[] = [];
  cycleLifes: CicloVida[] = [];
  isLoading: boolean = true;
  isEditMode: boolean = false;

  activeActive = true;
  activeFilter = 'A';
  paginatedVaccineApplications: VaccineApplications[] = [];
  pageSize = 10;
  currentPage = 1;

  applicationIdFilter: string = '';
  amountFilter: string = '';

  applicationForm: VaccineApplications = {
    cycleLifeId: undefined,
    shedId: undefined,
    endDate: '',
    times: '',
    dateRegistration: '',
    costApplication: 0,
    amount: undefined,
    quantityBirds: 0,
    active: 'A',
    email: ''
  };

  constructor(
    private vaccineApplicationsService: VaccineApplicationsService,
    private shedService: ShedService,
    private cicloVidaService: CicloVidaService
  ) {}

  ngOnInit(): void {
    this.getApplications();
    this.loadSheds(); // Cargar galpones
    this.loadCycles();
  }

  loadSheds(): void {
    this.shedService.getAll().subscribe(
      (data) => {
        this.sheds = data.filter(shed => shed.status === 'A'); // Filtrar galpones activos
      },
      (error) => console.error('Error al obtener galpones', error)
    );
  }

  loadCycles(): void {
    this.cicloVidaService.getCycles().subscribe(
      (cycles: CicloVida[]) => {
        console.log('Ciclos de vida obtenidos:', cycles);
        this.cycleLifes = cycles;

        if (this.cycleLifes.length === 0) {
          alert('No hay ciclos de vida disponibles.');
        } else if (!this.applicationForm.cycleLifeId) {
          this.applicationForm.cycleLifeId = this.cycleLifes[0].henId;
        }
      },
      (error) => {
        console.error('Error al cargar los ciclos de vida:', error);
        alert('No se pudieron cargar los ciclos de vida. Intente nuevamente más tarde.');
      }
    );
  }



  getApplications(): void {
    this.vaccineApplicationsService.vaccineApplications$.subscribe({
      next: (data: VaccineApplications[]) => {
        this.applications = data;
        this.filterApplications();
        this.isLoading = false;
      },
      error: (err: any) => {
        console.error('Error fetching applications:', err);
        this.isLoading = false;
      }
    });
  }

  onHenIdChange(): void {
    const cycleLifeId = this.applicationForm.cycleLifeId;
  
    if (cycleLifeId === undefined || !Number.isInteger(+cycleLifeId)) {
      alert('Por favor seleccione un Hen ID válido.');
      return;
    }
  
    const selectedCycle = this.cycleLifes.find(cycle => cycle.henId === +cycleLifeId);
    if (!selectedCycle) {
      alert('Hen ID no válido. Por favor seleccione uno de la lista.');
      return;
    }
  
  }
  

  getPages(): number[] {
    const totalPages = Math.ceil(
      this.filteredApplications.filter((app) => app.active === this.activeFilter).length / this.pageSize
    );
    return Array.from({ length: totalPages }, (_, i) => i + 1);
  }

  toggleActive(active: boolean): void {
    this.activeActive = active;
    this.activeFilter = active ? 'A' : 'I';
    this.filterApplications();
  }

  cambiarPagina(pagina: number): void {
    this.currentPage = pagina;
    this.applyActiveFilter();
  }

  applyActiveFilter(): void {
    const filteredApplications = this.filteredApplications.filter(
      (app) => app.active === this.activeFilter
    );

    this.paginatedVaccineApplications = filteredApplications.slice(
      (this.currentPage - 1) * this.pageSize,
      this.currentPage * this.pageSize
    );
  }

  

  filterApplications(): void {
    this.filteredApplications = this.applications.filter(application => {
      const matchesActive = application.active === this.activeFilter;
      const matchesId = application.applicationId?.toString().includes(this.applicationIdFilter);
      const matchesAmount = application.amount?.toString().includes(this.amountFilter);
      return matchesActive && (matchesId || matchesAmount);
    });
    this.applyActiveFilter(); // Aplicar filtro después de filtrar las aplicaciones
  }

  activateApplication(applicationId: number | undefined): void {
    if (applicationId !== undefined) {
      this.vaccineApplicationsService.activateVaccineApplications(applicationId).subscribe({
        next: () => {
          this.getApplications();
        },
        error: (err: any) => {
          console.error('Error activating application:', err);
        }
      });
    } else {
      console.error('Invalid application ID');
    }
  }

  inactivateApplication(applicationId: number | undefined): void {
    if (applicationId !== undefined) {
      this.vaccineApplicationsService.inactivateVaccineApplications(applicationId).subscribe({
        next: () => {
          this.getApplications();
        },
        error: (err: any) => {
          console.error('Error inactivating application:', err);
        }
      });
    } else {
      console.error('Invalid application ID');
    }
  }

  openModal(): void {
    this.isEditMode = false;
    this.applicationForm = {
      cycleLifeId: undefined,
      shedId: undefined,
      endDate: '',
      times: undefined,
      dateRegistration: '',
      amount: undefined,
      costApplication: 0,
      quantityBirds: 0,
      email: '',
      active: 'A'
    };
    
    this.isModalOpen = true;
  }

  editApplicationDetails(application: VaccineApplications): void {
    this.isEditMode = true;
    this.applicationForm = { ...application };
    this.isModalOpen = true;
  }

  closeModal(): void {
    this.isModalOpen = false;
  }

  addApplication(): void {
    if (this.applicationForm.cycleLifeId !== undefined) {
      const vaccineId = this.applicationForm.cycleLifeId;

      // Verificar valores de las fechas antes de la validación
      console.log('Fecha de Aplicación:', this.applicationForm.endDate);
      console.log('Fecha de Registro:', this.applicationForm.dateRegistration);

      // Validar endDate
      if (!this.applicationForm.endDate) {
        alert('La fecha de aplicación no puede estar vacía.');
        return;
      }

      // Validar dateRegistration
      if (!this.applicationForm.dateRegistration) {
        alert('La fecha de registro no puede estar vacía.');
        return;
      }

      // Formatear las fechas solo si existen
      this.applicationForm.endDate = formatDate(this.applicationForm.endDate, 'yyyy-MM-dd', 'en-US');
      this.applicationForm.dateRegistration = formatDate(this.applicationForm.dateRegistration, 'yyyy-MM-dd', 'en-US');

      // Verificar las fechas después de formatearlas
      console.log('Fecha de Aplicación Formateada:', this.applicationForm.endDate);
      console.log('Fecha de Registro Formateada:', this.applicationForm.dateRegistration);

      this.vaccineApplicationsService.createVaccineApplications(this.applicationForm).subscribe({
        next: () => {
          this.getApplications();
          this.filterApplications();
          this.closeModal();
        },
        error: (err: any) => {
          console.error('Error saving application:', err);
          alert('Error al guardar la aplicación.');
        }
      });
    } else {
      alert('Por favor, seleccione una vacuna.');
    }
  }

  updateApplication(): void {
    if (this.applicationForm.applicationId) {
      if (!this.applicationForm.cycleLifeId) {
        console.error('El ID de la vacuna no puede ser nulo');
        return;
      }

      // Check for null and format only if the date is not null
      if (this.applicationForm.endDate) {
        this.applicationForm.endDate = formatDate(this.applicationForm.endDate, 'yyyy-MM-dd', 'en-US');
      }

      if (this.applicationForm.dateRegistration) {
        this.applicationForm.dateRegistration = formatDate(this.applicationForm.dateRegistration, 'yyyy-MM-dd', 'en-US');
      }

      this.vaccineApplicationsService.updateVaccineApplications(this.applicationForm.applicationId, this.applicationForm).subscribe({
        next: () => {
          this.getApplications();
          this.closeModal();
        },
        error: (err: any) => {
          console.error('Error updating application:', err);
        }
      });
    }
  }

  formatDate(date: string | Date | null): string {
    if (date === null) {
      return ''; // You can return an empty string or some default value if the date is null
    }

    const parsedDate = typeof date === 'string' ? new Date(date) : date;
    const day = parsedDate.getDate();
    const monthNames = ['Ene', 'Feb', 'Mar', 'Abr', 'May', 'Jun', 'Jul', 'Ago', 'Sep', 'Oct', 'Nov', 'Dic'];
    const month = monthNames[parsedDate.getMonth()];
    const year = parsedDate.getFullYear();

    return `${day}-${month}-${year}`;
  }

  getNameIto(cycleLifeId: number | undefined, application: VaccineApplications): string {
    if (!application) return 'Desconocida';
  
    const cicloVida = this.cycleLifes.find(c => c.id=== cycleLifeId);
  
    return cicloVida?.nameIto ?? application.nameIto ?? 'Desconocida';
  }
  
  

  getShedName(shedId: number | undefined): string {
    const shed = this.sheds.find(v => v.id === shedId);
    return shed ? shed.name : 'Desconocida';
  }


  calculateTotal(cost: number | undefined, quantity: number | undefined): number {
    if (cost && quantity && quantity > 0) {
      return cost * quantity; // Cambiado a multiplicación para calcular el total
    }
    return 0;
  }
}
