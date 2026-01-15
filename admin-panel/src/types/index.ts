export interface AuthRequest {
  username: string;
  password: string;
}

export interface AuthResponse {
  token: string;
  username: string;
  roles: string[];
}

export interface TourDto {
  id?: number;
  name: string;
  description: string;
  price: number;
  durationDays: number;
  imageUrl: string;
  destinationCity: string;
  active: boolean;
  flightIds?: number[];
  createdAt?: string;
  updatedAt?: string;
}

export interface FlightDto {
  flightId?: number;
  flightNo: string;
  scheduledDeparture: string;
  scheduledArrival: string;
  departureAirportCode: string;
  arrivalAirportCode: string;
  status: string;
  aircraftCode: string;
  actualDeparture?: string;
  actualArrival?: string;
}

export interface ClientRequestDto {
  id?: number;
  tourId: number;
  userName: string;
  userEmail: string;
  userPhone?: string;
  comment?: string;
  status?: 'NEW' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED';
  priority?: 'NORMAL' | 'HIGH' | 'URGENT';
  createdAt?: string;
  tourName?: string;
  employeeId?: number;
  employeeName?: string;
}

export interface Page<T> {
  content: T[];
  pageable: {
    pageNumber: number;
    pageSize: number;
  };
  totalElements: number;
  totalPages: number;
  last: boolean;
  first: boolean;
  numberOfElements: number;
}

export interface ErrorResponse {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  path: string;
}

