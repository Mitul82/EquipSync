type Assets = {
    id: string,
    name:string,
    serialNumber: string,
    status: 'Available' | 'Assigned' | 'In_repair' | 'Retired',
    assignedTo: {
        id: string,
        email: string,
        department: string,
        role: 'Admin' | 'Manager' | 'Employee'
    },
    createdAt: string;
    updatedAt: string
}[];

export type { Assets }