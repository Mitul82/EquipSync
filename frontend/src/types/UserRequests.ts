type UserRequests = {
    id: string,
    requester: {
        id: string,
        email: string,
        department: string,
        role: 'Admin' | 'Manager' | 'Employee'
    },
    description: string,
    status: 'Pending' | 'Approved' | 'Denied' | 'Fullfilled',
    reviewedBy: {
        id: string,
        email: string,
        department: string,
        role: 'Admin' | 'Manager' | 'Employee'
    } | null,
    assignedAsset: {
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
    } | null,
    createdAt: string,
    updatedAt: string
}[];

export type { UserRequests }