import { useMemo, useState, type ReactNode } from 'react'
import {
  DndContext,
  KeyboardSensor,
  PointerSensor,
  closestCenter,
  type DragEndEvent,
  useSensor,
  useSensors,
} from '@dnd-kit/core'
import {
  SortableContext,
  arrayMove,
  sortableKeyboardCoordinates,
  useSortable,
  verticalListSortingStrategy,
} from '@dnd-kit/sortable'
import { CSS } from '@dnd-kit/utilities'
import { GripVertical } from 'lucide-react'
import { FormDrawer } from './FormDrawer'
import { cn } from '../../lib/utils/cn'

export type ReorderItem = {
  id: string
  label: string
  secondary?: string
  accentColor?: string
}

export interface ReorderDrawerProps {
  open: boolean
  title: string
  description?: string
  items: ReorderItem[]
  isLoading?: boolean
  isSubmitting?: boolean
  error?: unknown
  saveLabel?: string
  saveDisabled?: boolean
  emptyMessage?: string
  toolbar?: ReactNode
  width?: 'sm' | 'md' | 'lg'
  onClose: () => void
  onSave: (orderedIds: string[]) => void | Promise<void>
}

function SortableRow({ item }: { item: ReorderItem }) {
  const { attributes, listeners, setNodeRef, transform, transition, isDragging } = useSortable({
    id: item.id,
  })

  return (
    <li
      ref={setNodeRef}
      style={{
        transform: CSS.Transform.toString(transform),
        transition,
      }}
      className={cn(
        'flex items-center gap-3 rounded-lg border border-border bg-card px-3 py-2.5',
        isDragging && 'z-10 shadow-md ring-1 ring-primary/30',
      )}
    >
      <button
        type="button"
        className="cursor-grab touch-none rounded p-1 text-muted-foreground hover:bg-secondary hover:text-foreground active:cursor-grabbing"
        aria-label={`Drag to reorder ${item.label}`}
        {...attributes}
        {...listeners}
      >
        <GripVertical className="h-4 w-4" />
      </button>
      {item.accentColor ? (
        <span
          className="h-3.5 w-3.5 shrink-0 rounded border border-border"
          style={{ backgroundColor: item.accentColor }}
          aria-hidden
        />
      ) : null}
      <div className="min-w-0 flex-1">
        <p className="truncate text-sm font-medium text-foreground">{item.label}</p>
        {item.secondary ? (
          <p className="truncate font-mono text-xs text-muted-foreground">{item.secondary}</p>
        ) : null}
      </div>
    </li>
  )
}

export function ReorderDrawer({
  open,
  title,
  description,
  items,
  isLoading = false,
  isSubmitting = false,
  error,
  saveLabel = 'Save order',
  saveDisabled = false,
  emptyMessage = 'Nothing to reorder.',
  toolbar,
  width = 'md',
  onClose,
  onSave,
}: ReorderDrawerProps) {
  const [orderedIds, setOrderedIds] = useState<string[]>([])
  const [initialIds, setInitialIds] = useState<string[]>([])

  const sourceKey = open ? items.map((item) => item.id).join(',') : ''
  const [prevSourceKey, setPrevSourceKey] = useState(sourceKey)
  if (sourceKey !== prevSourceKey) {
    setPrevSourceKey(sourceKey)
    if (open) {
      const ids = items.map((item) => item.id)
      setOrderedIds(ids)
      setInitialIds(ids)
    }
  }

  const itemById = useMemo(() => {
    const map = new Map<string, ReorderItem>()
    items.forEach((item) => map.set(item.id, item))
    return map
  }, [items])

  const orderedItems = useMemo(
    () => orderedIds.map((id) => itemById.get(id)).filter((item): item is ReorderItem => Boolean(item)),
    [orderedIds, itemById],
  )

  const isDirty = useMemo(() => {
    if (orderedIds.length !== initialIds.length) {
      return true
    }
    return orderedIds.some((id, index) => id !== initialIds[index])
  }, [orderedIds, initialIds])

  const sensors = useSensors(
    useSensor(PointerSensor, { activationConstraint: { distance: 6 } }),
    useSensor(KeyboardSensor, { coordinateGetter: sortableKeyboardCoordinates }),
  )

  function handleDragEnd(event: DragEndEvent) {
    const { active, over } = event
    if (!over || active.id === over.id) {
      return
    }
    setOrderedIds((current) => {
      const oldIndex = current.indexOf(String(active.id))
      const newIndex = current.indexOf(String(over.id))
      if (oldIndex < 0 || newIndex < 0) {
        return current
      }
      return arrayMove(current, oldIndex, newIndex)
    })
  }

  return (
    <FormDrawer
      open={open}
      title={title}
      description={description}
      onClose={onClose}
      isSubmitting={isSubmitting}
      isDirty={isDirty}
      saveLabel={saveLabel}
      saveDisabled={saveDisabled || isLoading || orderedItems.length === 0 || !isDirty}
      error={error}
      width={width}
      onSubmit={() => {
        void onSave(orderedIds)
      }}
    >
      <div className="space-y-4">
        {toolbar}
        {isLoading ? (
          <p className="text-sm text-muted-foreground">Loading…</p>
        ) : orderedItems.length === 0 ? (
          <p className="text-sm text-muted-foreground">{emptyMessage}</p>
        ) : (
          <DndContext sensors={sensors} collisionDetection={closestCenter} onDragEnd={handleDragEnd}>
            <SortableContext items={orderedIds} strategy={verticalListSortingStrategy}>
              <ul className="space-y-2" aria-label="Reorder list">
                {orderedItems.map((item) => (
                  <SortableRow key={item.id} item={item} />
                ))}
              </ul>
            </SortableContext>
          </DndContext>
        )}
      </div>
    </FormDrawer>
  )
}
