import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Food } from '../model/food';
import { FoodInsert } from '../model/food';
import { FoodUpdate } from '../model/food';
import { BehaviorSubject, catchError, map, Observable, switchMap, tap, throwError } from 'rxjs';

@Injectable({
    providedIn: 'root'
})
export class FoodService {

    private apiUrl = 'https://8080-josegallardop-backendva-rtkkwwzk37v.ws-us118.gitpod.io/api/foods';

    // BehaviorSubject para mantener y compartir los datos de alimentación
    private foodSubject = new BehaviorSubject<Food[]>([]);
    public foods$ = this.foodSubject.asObservable();

    constructor(private http: HttpClient) { }

    getActiveFoods(): Observable<Food[]> {
        return this.http.get<Food[]>(`${this.apiUrl}/actives`);
    }

    getInactiveFoods(): Observable<Food[]> {
        return this.http.get<Food[]>(`${this.apiUrl}/inactives`);
    }

    getFoodsByType(foodType: string): Observable<Food[]> {
        return this.http.get<Food[]>(`${this.apiUrl}/type/${foodType}`);
    }


    addNewFood(food: FoodInsert): Observable<FoodInsert> {
        const headers = new HttpHeaders({ 'Content-Type': 'application/json' });
        return this.http.post<FoodInsert>(this.apiUrl, food, { headers });
    }

    updateFood(id_food: number, food: FoodUpdate): Observable<FoodUpdate> {
        return this.http.put<FoodUpdate>(`${this.apiUrl}/${id_food}`, food);
    }

    deactivateFood(id_food: number): Observable<string> {
        return this.http.put<string>(`${this.apiUrl}/delete/${id_food}`, {}, { responseType: 'text' as 'json' });
    }

    reactivateFood(id_food: number): Observable<string> {
        return this.http.put<string>(`${this.apiUrl}/restore/${id_food}`, {}, { responseType: 'text' as 'json' });
    }
}
