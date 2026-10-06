type AuthUser = {
    id: string,
    email: string,
    department: string,
    role: 'Admin' | 'Manager' | 'Employee'
}

export type { AuthUser }