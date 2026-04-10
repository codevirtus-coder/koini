import React, { useMemo, useState } from 'react';
import {
  ColumnDef,
  SortingState,
  flexRender,
  getCoreRowModel,
  getSortedRowModel,
  useReactTable,
} from '@tanstack/react-table';
import { cn } from '../../design/cn';
import { EmptyState } from '../ui/EmptyState';
import { TableSkeleton } from '../ui/Skeleton';
import { Pagination } from './Pagination';

interface Column<T> {
  key: keyof T;
  header: string;
  width?: string;
  render?: (value: T[keyof T], row: T) => React.ReactNode;
  sortable?: boolean;
  align?: 'left' | 'center' | 'right';
}

interface DataTableProps<T> {
  data: T[];
  columns: Column<T>[];
  isLoading?: boolean;
  emptyMessage?: string;
  emptyIcon?: React.ReactNode;
  pagination?: {
    page: number;
    totalPages: number;
    totalElements: number;
    onPageChange: (page: number) => void;
    size: number;
  };
  onRowClick?: (row: T) => void;
  rowKey: keyof T;
}

export function DataTable<T extends object>({
  data,
  columns,
  isLoading,
  emptyMessage,
  emptyIcon,
  pagination,
  onRowClick,
  rowKey,
}: DataTableProps<T>): JSX.Element {
  if (isLoading) return <TableSkeleton rows={pagination?.size ?? 5} />;

  if (!data || data.length === 0) {
    return <EmptyState title={emptyMessage ?? 'No data'} icon={emptyIcon} />;
  }

  const [sorting, setSorting] = useState<SortingState>([]);

  const columnDefs = useMemo<ColumnDef<T>[]>(() => {
    return columns.map((col) => ({
      accessorKey: String(col.key),
      header: col.header,
      cell: (info) => {
        const value = info.getValue() as T[keyof T];
        return col.render ? col.render(value, info.row.original) : String(value ?? '');
      },
      enableSorting: Boolean(col.sortable),
      meta: {
        align: col.align ?? 'left',
        width: col.width,
      },
    }));
  }, [columns]);

  const table = useReactTable({
    data,
    columns: columnDefs,
    state: { sorting },
    onSortingChange: setSorting,
    getRowId: (row) => String(row[rowKey]),
    getCoreRowModel: getCoreRowModel(),
    getSortedRowModel: getSortedRowModel(),
  });

  return (
    <div className="w-full overflow-x-auto">
      <table className="w-full text-sm border-separate border-spacing-0">
        <thead className="sticky top-0 bg-surface-bg">
          {table.getHeaderGroups().map((headerGroup) => (
            <tr key={headerGroup.id} className="text-[11px] uppercase tracking-wider text-text-muted border-b border-surface-border">
              {headerGroup.headers.map((header) => {
                const meta = header.column.columnDef.meta as { align?: string; width?: string } | undefined;
                const canSort = header.column.getCanSort();
                return (
                  <th
                    key={header.id}
                    className={cn(
                      'px-3 py-3 text-left bg-surface-bg',
                      meta?.align === 'right' && 'text-right',
                      canSort && 'select-none'
                    )}
                    style={{ width: meta?.width }}
                  >
                    {canSort ? (
                      <button
                        type="button"
                        className="inline-flex items-center gap-1 text-xs font-semibold text-text-muted hover:text-text-primary"
                        onClick={header.column.getToggleSortingHandler()}
                      >
                        {flexRender(header.column.columnDef.header, header.getContext())}
                        <span className="text-[10px]">
                          {header.column.getIsSorted() === 'asc' && '▲'}
                          {header.column.getIsSorted() === 'desc' && '▼'}
                          {!header.column.getIsSorted() && '↕'}
                        </span>
                      </button>
                    ) : (
                      flexRender(header.column.columnDef.header, header.getContext())
                    )}
                  </th>
                );
              })}
            </tr>
          ))}
        </thead>
        <tbody>
          {table.getRowModel().rows.map((row) => (
            <tr
              key={row.id}
              className={cn(
                'border-b border-surface-border odd:bg-surface-card even:bg-surface-bg',
                onRowClick && 'cursor-pointer hover:bg-surface-cardHover'
              )}
              onClick={onRowClick ? () => onRowClick(row.original) : undefined}
            >
              {row.getVisibleCells().map((cell) => {
                const meta = cell.column.columnDef.meta as { align?: string } | undefined;
                return (
                  <td key={cell.id} className={cn('px-3 py-3', meta?.align === 'right' && 'text-right')}>
                    {flexRender(cell.column.columnDef.cell, cell.getContext())}
                  </td>
                );
              })}
            </tr>
          ))}
        </tbody>
      </table>
      {pagination && (
        <Pagination page={pagination.page} totalPages={pagination.totalPages} onPageChange={pagination.onPageChange} />
      )}
    </div>
  );
}
