import { Radar, RadarChart as RechartsRadarChart, PolarGrid, PolarAngleAxis, PolarRadiusAxis, ResponsiveContainer, Legend } from 'recharts'

interface RadarChartData {
  subject: string
  A: number
  B: number
  C?: number
  fullMark: number
}

interface RadarChartProps {
  data: RadarChartData[]
  technologies: string[]
}

const colors = ['#a855f7', '#c084fc', '#d8b4fe', '#e9d5ff']

export default function RadarChart({ data, technologies }: RadarChartProps) {
  return (
    <div className="w-full h-80">
      <ResponsiveContainer width="100%" height="100%">
        <RechartsRadarChart data={data}>
          <PolarGrid 
            stroke="#a855f7" 
            strokeOpacity={0.3}
            className="dark:stroke-purple-400"
          />
          <PolarAngleAxis 
            dataKey="subject" 
            tick={{ 
              fill: '#7c3aed', 
              fontSize: 12,
              fontWeight: 500
            }}
            className="dark:fill-purple-300"
          />
          <PolarRadiusAxis 
            angle={90} 
            domain={[0, 100]} 
            tick={{ 
              fill: '#a855f7', 
              fontSize: 10 
            }}
            className="dark:fill-purple-400"
          />
          
          {technologies.map((tech, index) => {
            const dataKey = String.fromCharCode(65 + index) // A, B, C, etc.
            return (
              <Radar
                key={tech}
                name={tech}
                dataKey={dataKey}
                stroke={colors[index]}
                fill={colors[index]}
                fillOpacity={0.1}
                strokeWidth={2}
                dot={{ 
                  fill: colors[index], 
                  strokeWidth: 2, 
                  r: 4 
                }}
              />
            )
          })}
          
          <Legend 
            wrapperStyle={{
              paddingTop: '20px',
              fontSize: '14px',
              fontWeight: '500'
            }}
            iconType="circle"
          />
        </RechartsRadarChart>
      </ResponsiveContainer>
    </div>
  )
}