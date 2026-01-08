import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, Legend } from 'recharts'

interface TrendData {
  month: string
  [key: string]: string | number
}

interface TrendChartProps {
  data: TrendData[]
  technologies: string[]
  title: string
}

const colors = ['#a855f7', '#c084fc', '#d8b4fe', '#e9d5ff']

export default function TrendChart({ data, technologies, title }: TrendChartProps) {
  return (
    <div className="void-card rounded-xl p-6 glow-on-hover">
      <h4 className="text-lg font-semibold text-purple-800 dark:text-purple-200 mb-4">
        {title}
      </h4>
      <div className="w-full h-64">
        <ResponsiveContainer width="100%" height="100%">
          <LineChart data={data}>
            <CartesianGrid 
              strokeDasharray="3 3" 
              stroke="#a855f7" 
              strokeOpacity={0.2}
              className="dark:stroke-purple-400"
            />
            <XAxis 
              dataKey="month" 
              tick={{ 
                fill: '#7c3aed', 
                fontSize: 12 
              }}
              className="dark:fill-purple-300"
            />
            <YAxis 
              tick={{ 
                fill: '#7c3aed', 
                fontSize: 12 
              }}
              className="dark:fill-purple-300"
            />
            <Tooltip 
              contentStyle={{
                backgroundColor: 'rgba(255, 255, 255, 0.9)',
                border: '1px solid #a855f7',
                borderRadius: '8px',
                backdropFilter: 'blur(8px)'
              }}
              labelStyle={{ color: '#7c3aed' }}
            />
            <Legend 
              wrapperStyle={{
                paddingTop: '10px',
                fontSize: '12px'
              }}
            />
            
            {technologies.map((tech, index) => (
              <Line
                key={tech}
                type="monotone"
                dataKey={tech.toLowerCase()}
                stroke={colors[index]}
                strokeWidth={2}
                dot={{ 
                  fill: colors[index], 
                  strokeWidth: 2, 
                  r: 4 
                }}
                activeDot={{ 
                  r: 6, 
                  fill: colors[index],
                  stroke: '#fff',
                  strokeWidth: 2
                }}
              />
            ))}
          </LineChart>
        </ResponsiveContainer>
      </div>
    </div>
  )
}