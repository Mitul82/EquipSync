import toast from 'react-hot-toast';
import { useMutation, useQueryClient } from '@tanstack/react-query';

import api from '@/utils/api';

import type { AxiosError } from 'axios';
import type { ApiRes, AuthUser } from '@/types';

type UserData = {
    userId: string,
    role: 'Admin' | 'Manager' | 'Employee'
}

function useUpdateUser() {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: async (userData: UserData) => {
            const { data }: { data: ApiRes<AuthUser> } = await api.patch(`/user/update/${userData.userId}?role=${userData.role}`);

            return data;
        },
        onMutate: () => {
            toast.loading('Updating user...', { id: 'updateUser-toast' });
        },
        onSuccess: (data: ApiRes<AuthUser>) => {
            toast.success(data.message || 'User updated', { id: 'updateUser-toast' });

            queryClient.invalidateQueries({ queryKey: ['getAllUsers'] });
        },
        onError: (err: AxiosError<{ message: string }>) => {
            const errMessage = err.response?.data.message || 'Error in updating the user';

            toast.error(errMessage, { id: 'updateUser-toast' });
        }
    });
}

export default useUpdateUser;