import React from 'react';
import { UserCreateForm } from '../../../components/forms/UserCreateForm';

export default function CreateUserPage(): JSX.Element {
  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold text-text-primary">Create User</h1>
      <UserCreateForm />
    </div>
  );
}
